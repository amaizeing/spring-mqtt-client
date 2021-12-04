package io.github.amaizeing.mqtt.core.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {

  private String firstName;
  private String lastName;
  private UniversityDto university;

}
