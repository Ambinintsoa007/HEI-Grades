package com.example.demo.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentCourseEnrollment {
  private final UUID id;
  private final UUID studentId;
  private final UUID courseId;
  private final UUID academicYearId;
  @Builder.Default private final Set<UUID> courseOfferingIds = Set.of();
  private final Instant enrolledAt;
}
