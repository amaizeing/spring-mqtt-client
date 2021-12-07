package io.github.amaizeing.mqtt.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MessageChunk {

  private final Completer ack;
  private final byte[] payload;
}
