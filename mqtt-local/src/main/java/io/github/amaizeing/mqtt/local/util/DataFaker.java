package io.github.amaizeing.mqtt.local.util;

import com.github.javafaker.Faker;
import io.github.amaizeing.mqtt.local.dto.ClassDto;
import io.github.amaizeing.mqtt.local.dto.StudentDto;
import io.github.amaizeing.mqtt.local.dto.UniversityDto;
import lombok.val;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataFaker {

  private static final Faker FAKER = new Faker();

  private DataFaker() {
  }

  public static StudentDto newStudent() {
    return StudentDto.builder()
        .firstName(FAKER.name().firstName())
        .lastName(FAKER.name().lastName())
        .fullName(FAKER.name().fullName())
        .phoneNumber(FAKER.phoneNumber().phoneNumber())
        .bloodType(FAKER.name().bloodGroup())
        .fullAddress(FAKER.address().fullAddress())
        .birthday(FAKER.date().birthday(18, 24))
        .build();
  }

  public static ClassDto newClass(int studentSize) {
    val students = IntStream.range(0, studentSize)
        .mapToObj(i -> newStudent())
        .collect(Collectors.toList());
    return ClassDto.builder()
        .name(FAKER.ancient().hero())
        .buildingNo(FAKER.address().buildingNumber())
        .floor(FAKER.random().nextInt(1, 10))
        .squareSize(FAKER.random().nextInt(50, 100) * 1.1f)
        .students(students)
        .build();
  }

  public static UniversityDto newUniversity(int classSize, int minStudentSize, int maxStudentSize) {
    val classes = IntStream.range(0, classSize)
        .mapToObj(i -> newClass(FAKER.random().nextInt(minStudentSize, maxStudentSize)))
        .collect(Collectors.toList());
    return UniversityDto.builder()
        .name(FAKER.university().name())
        .fullAddress(FAKER.address().fullAddress())
        .phoneNumber(FAKER.phoneNumber().phoneNumber())
        .classes(classes)
        .build();
  }


}
