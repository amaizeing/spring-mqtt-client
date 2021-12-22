package io.github.amaizeing.mqtt.core.config;

import io.github.amaizeing.mqtt.core.Compressor;
import io.github.amaizeing.mqtt.core.Serializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.net.SocketFactory;

@Slf4j
@Getter
@Builder
@AllArgsConstructor
public class MqttConfig {

  private static final int DEFAULT_COMPRESS_THRESHOLD = 10 * 1024;
  private static final int DEFAULT_CHUNK_SIZE = 80 * 1024;

  @Builder.Default
  private int compressThreshold = DEFAULT_COMPRESS_THRESHOLD;
  @Builder.Default
  private int chunkSize = DEFAULT_CHUNK_SIZE;
  @Builder.Default
  private boolean forkMessage = true;
  @Builder.Default
  private Serializer serializer = Serializer.JSON_SERIALIZER;
  @Builder.Default
  private int maxInFlight = 5_000;
  @Builder.Default
  private int disconnectBufferSize = 10_000;
  private String clientId;
  private String clientEndpoint;
  private SocketFactory socketFactory;
  @Builder.Default
  private Compressor compressor = Compressor.NO_COMPRESS;
  @Builder.Default
  private boolean cleanSession = false;

}
