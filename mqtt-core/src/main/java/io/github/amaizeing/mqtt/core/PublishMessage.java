package io.github.amaizeing.mqtt.core;

import com.fasterxml.jackson.annotation.JsonProperty;
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

  @JsonProperty("d")
  private String messageId;
  @JsonProperty("c")
  private boolean compress;
  @JsonProperty("p")
  private byte[] payload;
  @JsonProperty("s")
  private int messageSize;

  /**
   * Message index in case fork message. Start from 0.
   */
  @JsonProperty("i")
  private int index;
  @JsonProperty("t")
  private int totalMessages;
  @JsonProperty("j")
  private String combinedMessageId;
  @JsonProperty("g")
  private boolean ping;

  boolean isFork() {
    return totalMessages > 1;
  }

}
