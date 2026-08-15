package com.example.demo.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseOffering {
  private final UUID id;
  private final UUID courseId;
  private final UUID academicYearId;
  private final UUID groupId;
}
