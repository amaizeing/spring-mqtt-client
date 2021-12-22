package io.github.amaizeing.mqtt.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PACKAGE)
@AllArgsConstructor
public class Pair<L, R> {

  private L left;
  private R right;

  void set(L left, R right) {
    setLeft(left);
    setRight(right);
  }

}
