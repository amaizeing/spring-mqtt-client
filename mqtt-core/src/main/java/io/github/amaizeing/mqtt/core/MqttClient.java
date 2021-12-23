package io.github.amaizeing.mqtt.core;

import io.github.amaizeing.mqtt.core.config.MqttConfig;
import io.github.amaizeing.mqtt.core.exception.CompressException;
import io.github.amaizeing.mqtt.core.exception.MessageBrokerException;
import io.github.amaizeing.mqtt.core.exception.SerializeException;
import io.github.amaizeing.mqtt.core.util.ArrayUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.util.CollectionUtils;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

@Slf4j
public final class MqttClient implements PubSubClient {

  private final AtomicLong disconnectedTimeInMs;
  private final Map<String, Map<Integer, MessageChunk>> combinedIdToPayloads;
  private final MqttAsyncClient client;
  private final Serializer serializer;
  private final Compressor compressor;
  private final boolean forkMessage;

  private final int chunkSize;
  private final int compressThreshold;

  private final Pair<String, Boolean> lastMessageIdIsReceiveCompleted;
  private final Lock publishLock;

  private long publishedMessageCount = 0;

  private final Set<String> processingMessageId;

  public MqttClient(final MqttConfig config) throws MqttException {

    publishLock = new ReentrantLock();

    forkMessage = config.isForkMessage();
    chunkSize = config.getChunkSize();
    compressThreshold = config.getCompressThreshold();
    disconnectedTimeInMs = new AtomicLong(0);
    lastMessageIdIsReceiveCompleted = new Pair<>(null, null);

    serializer = config.getSerializer();
    compressor = config.getCompressor();

    processingMessageId = new HashSet<>();
    combinedIdToPayloads = new ConcurrentHashMap<>();

    val options = new MqttConnectOptions();
    options.setMaxInflight(config.getMaxInFlight());
    options.setCleanSession(config.isCleanSession());
    options.setAutomaticReconnect(true);
    options.setMaxReconnectDelay(8_000);

    val socketFactory = config.getSocketFactory();
    final String endPoint;
    if (socketFactory != null) {
      options.setSocketFactory(config.getSocketFactory());
      endPoint = "ssl://" + config.getClientEndpoint();
    } else {
      endPoint = "tcp://" + config.getClientEndpoint();
    }

    client = new MqttAsyncClient(endPoint, config.getClientId(), new MemoryPersistence());

    val disconnectOptions = new DisconnectedBufferOptions();
    disconnectOptions.setBufferEnabled(true);
    disconnectOptions.setDeleteOldestMessages(false);
    disconnectOptions.setBufferSize(config.getDisconnectBufferSize());
    client.setBufferOpts(disconnectOptions);
    client.setManualAcks(true);
    client.setCallback(new MqttCallbackExtended() {
      @Override
      public void connectComplete(final boolean reconnect, final String serverUri) {
        if (reconnect) {
          val time = System.currentTimeMillis() - disconnectedTimeInMs.getAndSet(0);
          log.info("Reconnect successful to: {} with clientId: {} after: {} ms",
              serverUri, config.getClientId(), time);
          return;
        }
        log.info("Connect successful to: {} with clientId: {}", serverUri, config.getClientId());
      }

      @Override
      public void connectionLost(final Throwable cause) {
        disconnectedTimeInMs.set(System.currentTimeMillis());
        log.error("Lost connection to MQTT Server: {}", endPoint, cause);
      }

      @Override
      public void messageArrived(final String topic, final MqttMessage message) {
        log.info("Received message from topic: {} with id: {}", topic, message.getId());
      }

      @Override
      public void deliveryComplete(final IMqttDeliveryToken token) {
        // Do nothing
      }
    });
    log.info("Connecting to MQTT Server: {}", endPoint);
    while (true) {
      try {
        val token = client.connect(options);
        token.waitForCompletion();
        break;
      } catch (Exception ex) {
        log.error("Exception while connecting to MQTT Broker", ex);
      }
    }

  }

  @Override
  public boolean isConnected() {
    return client.isConnected();
  }

  @Override
  public <T> void publish(final String topic, final T data)
      throws IllegalArgumentException, SerializeException, MessageBrokerException {

    val bytes = serializer.serialize(data);

    if (data instanceof ConnectionMessage) {
      publishConnectionMessage(topic, bytes);
      return;
    }

    if (bytes.length < compressThreshold) {
      publishInThreshold(topic, bytes);
      return;
    }
    publishOutThreshold(topic, bytes);
  }

  private void publishConnectionMessage(String topic, final byte[] bytes) throws MessageBrokerException {
    val message = PublishMessage.builder()
        .payload(bytes)
        .ping(true)
        .build();
    try {
      publishLock.lock();
      publishMessage(topic, message);
    } catch (MqttException ex) {
      throw new MessageBrokerException(ex);
    } finally {
      publishLock.unlock();
    }
  }

  private void publishInThreshold(String topic, final byte[] bytes)
      throws SerializeException, MessageBrokerException {
    val message = PublishMessage.builder()
        .messageId(UUID.randomUUID().toString())
        .payload(bytes)
        .messageSize(bytes.length)
        .totalMessages(1)
        .compress(false)
        .build();
    try {
      publishLock.lock();
      publishMessage(topic, message);
    } catch (MqttException ex) {
      throw new MessageBrokerException(ex);
    } finally {
      publishLock.unlock();
    }
  }

  private void publishOutThreshold(String topic, final byte[] bytes)
      throws IllegalArgumentException, MessageBrokerException {
    try {
      val compressedBytes = compressor.compress(bytes);
      val arrays = ArrayUtil.splitArray(compressedBytes, chunkSize);
      if (!forkMessage && arrays.size() > 1) {
        log.error("Message size after compressing is still over: {} bytes", chunkSize);
        throw new IllegalArgumentException("Message size is over, please enable fork message");
      }

      val zipMessageId = UUID.randomUUID().toString();

      try {
        publishLock.lock();
        IntStream.range(0, arrays.size())
            .mapToObj(i -> PublishMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .compress(true)
                .combinedMessageId(zipMessageId)
                .totalMessages(arrays.size())
                .messageSize(compressedBytes.length)
                .index(i)
                .payload(arrays.get(i))
                .build())
            .forEach(message -> {
              try {
                publishMessage(topic, message);
              } catch (Exception ex) {
                log.error("Exception while publishing message to MQTT", ex);
              }
            });
      } finally {
        publishLock.unlock();
      }
    } catch (CompressException ex) {
      throw new MessageBrokerException(ex);
    }
  }

  @Override
  public <T> void subscribe(final String topicName, final Subscriber<T> onReceive) throws MessageBrokerException {
    try {
      client.subscribe(topicName, 1, (topic, mqttMessage) -> {

        val message = serializer.deserialize(mqttMessage.getPayload(), PublishMessage.class);
        val messageId = message.getMessageId();

        val ackSender = new Completer(() -> {
          try {
            client.messageArrivedComplete(mqttMessage.getId(), mqttMessage.getQos());
            if (!message.isPing()) {
              log.debug("Sending ACK to server with messageId: {}", messageId);
            }
          } catch (MqttException ex) {
            log.info(ex.getMessage(), ex);
          }
          processingMessageId.remove(messageId);
        });

        try {
          if (message.isFork()) {
            subscribeOnCombineMessage(topic, message, ackSender, onReceive);
            return;
          }
          subscribeOnSingleMessage(topic, message, ackSender, onReceive);
        } catch (Exception ex) {
          processingMessageId.remove(messageId);
          if (onReceive.retryOnError()) {
            throw ex;
          }
        }
      });
    } catch (MqttException ex) {
      throw new MessageBrokerException(ex);
    }
  }

  private <T> void subscribeOnSingleMessage(final String topic,
      final PublishMessage message,
      final Completer ackSender,
      final Subscriber<T> onReceive) throws CompressException {

    val messageId = message.getMessageId();

    log.info("Received message from topic: {} with messageId: {}", topic, messageId);
    if (!processingMessageId.add(messageId)) {
      log.error("Ignore duplicate message (may be disconnect issue) with messageId: {}", messageId);
      return;
    }

    if (lastMessageIdIsReceiveCompleted.getRight() != null && !lastMessageIdIsReceiveCompleted.getRight()) {
      val combinedId = lastMessageIdIsReceiveCompleted.getLeft();
      log.error("This client is disconnected when receiving message (fork) with combinedId: {}", combinedId);
      val messages = combinedIdToPayloads.remove(combinedId);
      if (!CollectionUtils.isEmpty(messages)) {
        messages.values().forEach(m -> m.getAck().complete());
      }
    }

    if (!message.isCompress()) {
      onReceive.consume(serializer, messageId, message.getPayload(), ackSender);
      return;
    }
    val decompressBytes = compressor.decompress(message.getPayload());
    onReceive.consume(serializer, messageId, decompressBytes, ackSender);
    lastMessageIdIsReceiveCompleted.set(messageId, true);
  }

  private <T> void subscribeOnCombineMessage(final String topic,
      final PublishMessage message,
      final Completer ackSender,
      final Subscriber<T> onReceive) throws CompressException {

    val combinedMessageId = message.getCombinedMessageId();
    val totalMessageCount = message.getTotalMessages();

    log.info("Receive messageId: {} with combinedId: {}", message.getMessageId(), combinedMessageId);

    val indexToPayload = combinedIdToPayloads
        .computeIfAbsent(message.getCombinedMessageId(), k -> new TreeMap<>());

    if (indexToPayload.containsKey(message.getIndex())) {
      log.warn("Receive duplicate split message with combinedId: {}", combinedMessageId);
      return;
    }

    if (message.getIndex() > indexToPayload.size()) {
      log.info("message index: {}", message.getIndex());
      log.warn("This may be new client -> ignore message with combinedId: {}", combinedMessageId);
      for (val value : indexToPayload.values()) {
        value.getAck().complete();
      }
      ackSender.complete();
      indexToPayload.clear();
      return;
    }

    val messageChunk = MessageChunk.builder()
        .ack(ackSender)
        .payload(message.getPayload())
        .build();
    indexToPayload.put(message.getIndex(), messageChunk);

    if (lastMessageIdIsReceiveCompleted.getRight() != null && !lastMessageIdIsReceiveCompleted.getRight()) {
      val combinedId = lastMessageIdIsReceiveCompleted.getLeft();
      if (!combinedMessageId.equals(combinedId)) {
        lastMessageIdIsReceiveCompleted.set(combinedMessageId, false);
        log.error("This client is disconnected when receiving message (fork) with messageId: {} and combinedId: {}",
            message.getMessageId(), combinedId);
        val messages = combinedIdToPayloads.remove(combinedId);
        if (CollectionUtils.isEmpty(messages)) {
          return;
        }
        messages.values().forEach(m -> m.getAck().complete());
        return;
      }
    }
    lastMessageIdIsReceiveCompleted.set(combinedMessageId, false);

    if (indexToPayload.size() < totalMessageCount) {
      log.info("Waiting {} message(s) before joining with combinedId: {}",
          totalMessageCount - indexToPayload.size(), combinedMessageId);
      return;
    }

    val byteBuffer = ByteBuffer.allocate(message.getMessageSize());
    val ackSenders = new LinkedList<Completer>();
    indexToPayload.values()
        .forEach(m -> {
          byteBuffer.put(m.getPayload());
          ackSenders.add(m.getAck());
        });
    val fullPayload = byteBuffer.array();
    val decompressPayload = compressor.decompress(fullPayload);
    indexToPayload.clear();
    combinedIdToPayloads.remove(message.getCombinedMessageId());

    if (!processingMessageId.add(combinedMessageId)) {
      log.error("Ignore duplicate (fork) message with combinedId: {}", combinedMessageId);
      return;
    }

    log.info("Received full message (fork) to topic: {} with combinedId: {}", topic, combinedMessageId);
    onReceive.consume(serializer, combinedMessageId, decompressPayload,
        new Completer(() -> {
          ackSenders.forEach(Completer::complete);
          processingMessageId.remove(combinedMessageId);
        }));
    lastMessageIdIsReceiveCompleted.set(combinedMessageId, true);
  }

  @SneakyThrows
  private void publishMessage(final String topic, final PublishMessage message) throws MqttException {
    try {
      ++publishedMessageCount;
      log.debug("Publish message: {} th in this instance", publishedMessageCount);
      client.publish(topic, serializer.serialize(message), 1, false);
    } finally {
      if (message.getCombinedMessageId() == null) {
        log.info("Publishing message to topic: {} with messageId: {}", topic, message.getMessageId());
      } else {
        log.info("Publishing message (fork) to topic: {} with messageId: {} and combinedId: {}",
            topic, message.getMessageId(), message.getCombinedMessageId());
      }
      log.info("Inflight message count: {}, buffer message count: {}",
          client.getInFlightMessageCount(), client.getBufferedMessageCount());
    }
  }

  @Override
  public void unsubscribe(final String topic) throws MessageBrokerException {
    try {
      client.unsubscribe(topic);
    } catch (MqttException ex) {
      throw new MessageBrokerException(ex);
    }
  }

  @Override
  public void unsubscribe(final Collection<String> topics) throws MessageBrokerException {
    if (CollectionUtils.isEmpty(topics)) {
      return;
    }

    try {
      client.unsubscribe(topics.toArray(new String[0]));
    } catch (MqttException ex) {
      throw new MessageBrokerException(ex);
    }
  }

}
