package com.example.demo.endpoint.rest.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentCourseResponse {

  private final UUID enrollmentId;
  private final UUID courseId;
  private final String ref;
  private final String title;
  private final Integer credits;
  private final UUID academicYearId;
  private final Instant enrolledAt;
}
