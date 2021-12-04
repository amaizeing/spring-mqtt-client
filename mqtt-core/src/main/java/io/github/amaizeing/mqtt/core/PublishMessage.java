package io.github.amaizeing.mqtt.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PACKAGE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
final class PublishMessage {

  private String messageId;
  private boolean zip;
  private byte[] payload;
  private int messageSize;

  private String combinedMessageId;
  private int index;
  private int totalMessages;

  boolean isSplit() {
    return totalMessages > 1;
  }

}
