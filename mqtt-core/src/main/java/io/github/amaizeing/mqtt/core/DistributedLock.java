package io.github.amaizeing.mqtt.core;

public interface DistributedLock {

  void lock(String key);

  void unlock(String key);

  void tryLock(String key);

}
