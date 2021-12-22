package io.github.amaizeing.mqtt.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class CloudApplication {

  public static void main(String[] args) {
    SpringApplication.run(CloudApplication.class, args);
  }

}
