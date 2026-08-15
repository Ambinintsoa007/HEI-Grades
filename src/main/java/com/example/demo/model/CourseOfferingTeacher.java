package com.example.demo.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseOfferingTeacher {
  private final UUID id;
  private final UUID courseOfferingId;
  private final UUID teacherId;
}
