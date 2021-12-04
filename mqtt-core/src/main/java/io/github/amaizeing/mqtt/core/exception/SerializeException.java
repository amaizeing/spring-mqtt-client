package io.github.amaizeing.mqtt.core.exception;

public class SerializeException extends RuntimeException {

  public SerializeException(final String message) {
    super(message);
  }

  public SerializeException(final Throwable cause) {
    super(cause);
  }

}
