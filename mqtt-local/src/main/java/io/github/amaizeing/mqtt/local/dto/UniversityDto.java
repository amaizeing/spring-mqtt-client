package io.github.amaizeing.mqtt.local.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UniversityDto {

  private String name;
  private String fullAddress;
  private String phoneNumber;
  private List<ClassDto> classes = new LinkedList<>();


  public UniversityDto addClass(ClassDto classDto) {
    classes.add(classDto);
    return this;
  }

}
