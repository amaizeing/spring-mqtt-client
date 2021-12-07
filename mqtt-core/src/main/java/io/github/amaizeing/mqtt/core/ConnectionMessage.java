package io.github.amaizeing.mqtt.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class ConnectionMessage {

  @JsonProperty("i")
  private String identity;
  @JsonProperty("u")
  private long utc;

  public static ConnectionMessage create(String identity) {
    return new ConnectionMessage(identity, System.currentTimeMillis());
  }

}
