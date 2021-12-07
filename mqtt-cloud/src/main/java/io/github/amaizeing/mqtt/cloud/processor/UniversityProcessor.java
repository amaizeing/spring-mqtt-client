package io.github.amaizeing.mqtt.cloud.processor;

import io.github.amaizeing.mqtt.cloud.dto.UniversityDto;
import io.github.amaizeing.mqtt.cloud.service.ClassService;
import io.github.amaizeing.mqtt.cloud.service.StudentService;
import io.github.amaizeing.mqtt.cloud.service.UniversityService;
import io.github.amaizeing.mqtt.core.actor.ProcessorContext;
import io.github.amaizeing.mqtt.core.actor.Processor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UniversityProcessor extends Processor<UniversityDto> {

  private final ClassService classService;
  private final StudentService studentService;
  private final UniversityService universityService;

  @Override
  protected void process(final ProcessorContext<UniversityDto> context) throws Exception {
    log.info("Processing UniversityDto...............");
    Thread.sleep(500);
    classService.doSomething();
    studentService.doSomething();
    universityService.doSomething();
    context.put("key", "hello");
    log.info("Processing UniversityDto complete");
  }

}
