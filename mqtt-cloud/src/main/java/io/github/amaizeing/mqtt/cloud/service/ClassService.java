package io.github.amaizeing.mqtt.cloud.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClassService {

  public void doSomething() {
    log.info("Do some task in ClassService");
  }

}
