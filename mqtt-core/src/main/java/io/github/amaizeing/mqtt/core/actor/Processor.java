package io.github.amaizeing.mqtt.core.actor;

public abstract class Processor<T> {

  protected abstract void process(ProcessorContext<T> processorContext) throws Exception;

}
