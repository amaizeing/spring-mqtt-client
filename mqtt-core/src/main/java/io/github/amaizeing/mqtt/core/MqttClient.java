package io.github.amaizeing.mqtt.core;

import io.github.amaizeing.mqtt.core.config.MqttConfig;
import io.github.amaizeing.mqtt.core.util.ArrayUtil;
import io.github.amaizeing.mqtt.core.util.Zlib;
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.UnexpectedException;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

@Slf4j
public final class MqttClient implements MessageBrokerClient {

  private static final int COMPRESS_THRESHOLD = 10 * 1024;
  private static final int CHUNK_SIZE = 80 * 1024;

  private final AtomicLong disconnectedTimeInMs = new AtomicLong(0);
  private final Map<String, Map<Integer, byte[]>> zipIdToPayloads;
  private final MqttAsyncClient client;
  private final Serializer serializer;
  private final boolean splitMessage;

  public MqttClient(MqttConfig config) throws MqttException {

    val configSerializer = config.getSerializer();
    serializer = Objects.requireNonNullElseGet(configSerializer, JsonSerializer::new);
    zipIdToPayloads = new ConcurrentHashMap<>();
    splitMessage = config.isSplitMessage();

    val options = new MqttConnectOptions();
    options.setMaxInflight(config.getMaxInFlight());
    options.setCleanSession(false);
    options.setAutomaticReconnect(true);
    options.setMaxReconnectDelay(8000);

    if (config.isSsl()) {
      options.setSocketFactory(config.getSocketFactory());
    }

    val endPoint = getEndpoint(config);
    client = new MqttAsyncClient(endPoint, config.getClientId(), new MemoryPersistence());

    val disconnectOptions = new DisconnectedBufferOptions();
    disconnectOptions.setBufferEnabled(true);
    disconnectOptions.setDeleteOldestMessages(false);
    disconnectOptions.setBufferSize(1_000);
    client.setBufferOpts(disconnectOptions);

    client.setCallback(new MqttCallbackExtended() {
      @Override
      public void connectComplete(final boolean reconnect, final String serverUri) {
        if (reconnect) {
          val time = System.currentTimeMillis() - disconnectedTimeInMs.getAndSet(0);
          log.info("Reconnect successful to: ({}) with clientId: ({}) after: {} ms",
                   serverUri, config.getClientId(), time);
          return;
        }
        log.info("Connect successful to: ({}) with clientId: ({})", serverUri, config.getClientId());
      }

      @Override
      public void connectionLost(final Throwable cause) {
        disconnectedTimeInMs.set(System.currentTimeMillis());
        log.error("Lost connection to MQTT Server: ({})", endPoint, cause);
      }

      @Override
      public void messageArrived(final String topic, final MqttMessage message) {
        log.info("Received message to topic: ({}) with id: ({})", topic, message.getId());
      }

      @Override
      public void deliveryComplete(final IMqttDeliveryToken token) {
        log.info("Published message with id: ({})", token.getMessageId());
      }
    });
    client.connect(options);
  }

  String getEndpoint(final MqttConfig config) {
    if (config.isSsl()) {
      return "ssl://" + config.getClientEndpoint();
    }
    return "tcp://" + config.getClientEndpoint();
  }


  @Override
  public <T> void publish(final String topic, final T data) throws IllegalArgumentException {

    var dataBytes = serializer.serialize(data);
    if (dataBytes.length < COMPRESS_THRESHOLD) {
      publishInThreshold(topic, dataBytes);
      return;
    }
    publishOutThreshold(topic, dataBytes);
  }

  private void publishInThreshold(String topic, final byte[] dataBytes) {
    val message = PublishMessage.builder()
        .messageId(UUID.randomUUID().toString())
        .payload(dataBytes)
        .messageSize(dataBytes.length)
        .totalMessages(1)
        .zip(false)
        .build();
    try {
      client.publish(topic, new MqttMessage(serializer.serialize(message)));
    } catch (MqttException ex) {
      log.error("Exception while publishing message to MQTT", ex);
    }
  }

  private void publishOutThreshold(String topic, final byte[] dataBytes) {
    try {
      val compressDataBytes = Zlib.compress(dataBytes);
      val arrays = ArrayUtil.splitArray(compressDataBytes, CHUNK_SIZE);
      if (!splitMessage && arrays.size() > 1) {
        log.error("Message size after compressing is still over: {} bytes", CHUNK_SIZE);
        throw new IllegalArgumentException("Message size is over");
      }
      val zipMessageId = UUID.randomUUID().toString();
      IntStream.range(0, arrays.size())
          .mapToObj(i -> PublishMessage.builder()
              .messageId(UUID.randomUUID().toString())
              .zip(true)
              .combinedMessageId(zipMessageId)
              .totalMessages(arrays.size())
              .messageSize(compressDataBytes.length)
              .index(i)
              .payload(arrays.get(i))
              .build())
          .forEach(msg -> {
            try {
              client.publish(topic, new MqttMessage(serializer.serialize(msg)));
            } catch (MqttException ex) {
              log.error("Exception while publishing message to MQTT", ex);
            }
          });
    } catch (IOException ex) {
      log.error("Exception while publishing message to MQTT", ex);
    }
  }


  public <T> void subscribe(final String topicName, final Consumer<T> onReceive) {
    try {
      client.subscribe(topicName, 1, (topic, payload) -> {
        val message = serializer.deserialize(payload.getPayload(), PublishMessage.class);
        if (!message.isSplit()) {
          if (!message.isZip()) {
            onReceive.consume(serializer, message.getMessageId(), message.getPayload());
            return;
          }
          onReceive.consume(serializer, message.getMessageId(), Zlib.decompress(message.getPayload()));
          return;
        }

        val size = message.getTotalMessages();
        var indexToPayload = zipIdToPayloads
            .computeIfAbsent(message.getCombinedMessageId(), k -> new TreeMap<>());
        if (indexToPayload.containsKey(message.getIndex())) {
          log.error("Duplicate message...", new UnexpectedException("Duplicate message"));
          return;
        }
        indexToPayload.put(message.getIndex(), message.getPayload());
        if (indexToPayload.size() != size) {
          return;
        }

        val byteBuffer = ByteBuffer.allocate(message.getMessageSize());
        indexToPayload.values().forEach(byteBuffer::put);
        val fullPayload = byteBuffer.array();
        val decompressPayload = Zlib.decompress(fullPayload);
        indexToPayload.clear();

        onReceive.consume(serializer, message.getMessageId(), decompressPayload);
      });
    } catch (MqttException e) {
      log.error(e.getMessage(), e);
      // TODO
    }
  }

}
