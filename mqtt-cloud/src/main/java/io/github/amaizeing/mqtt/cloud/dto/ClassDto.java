package io.github.amaizeing.mqtt.cloud.dto;

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
public class ClassDto {

  private String name;
  private String buildingNo;
  private int floor;
  private float squareSize;
  private List<StudentDto> students = new LinkedList<>();

  public ClassDto addStudent(StudentDto student) {
    students.add(student);
    return this;
  }

}
