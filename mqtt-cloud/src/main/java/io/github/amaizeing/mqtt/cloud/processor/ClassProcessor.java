package io.github.amaizeing.mqtt.cloud.processor;

import io.github.amaizeing.mqtt.core.actor.Processor;
import io.github.amaizeing.mqtt.core.actor.ProcessorContext;
import io.github.amaizeing.mqtt.cloud.dto.ClassDto;
import io.github.amaizeing.mqtt.cloud.service.ClassService;
import io.github.amaizeing.mqtt.cloud.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassProcessor extends Processor<ClassDto> {

  private final ClassService classService;
  private final StudentService studentService;

  @Override
  protected void process(final ProcessorContext<ClassDto> context) throws InterruptedException {
    log.info("Processing ClassDto...............");
    Thread.sleep(5_000);
    studentService.doSomething();
    classService.doSomething();
    log.info("Processing ClassDto complete");
  }

}
