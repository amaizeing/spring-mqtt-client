package io.github.amaizeing.mqtt.cloud.listener;

import io.github.amaizeing.mqtt.core.BiConnection;
import io.github.amaizeing.mqtt.core.Subscriber;
import io.github.amaizeing.mqtt.core.CoreSubscriber;
import io.github.amaizeing.mqtt.core.PubSubClientFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Loader extends CoreSubscriber {

  protected Loader(final List<Subscriber<?>> subscribers,
      final List<BiConnection> biConnections,
      final PubSubClientFactory clientFactory) {
    super(subscribers, biConnections, clientFactory);
  }

}
