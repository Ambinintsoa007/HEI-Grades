package com.example.demo.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Grade {
  private final UUID id;
  private final UUID examId;
  private final UUID studentCourseEnrollmentId;
  private final BigDecimal score;
  private final Instant updatedAt;
}
