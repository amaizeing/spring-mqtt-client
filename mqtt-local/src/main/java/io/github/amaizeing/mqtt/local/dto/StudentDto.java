package io.github.amaizeing.mqtt.local.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {

  private String firstName;
  private String lastName;
  private String fullName;
  private String phoneNumber;
  private String bloodType;
  private String fullAddress;
  private Date birthday;

}
