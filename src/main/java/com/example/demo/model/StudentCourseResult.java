package com.example.demo.model;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentCourseResult {
  private final UUID enrollmentId;
  private final UUID courseId;
  private final UUID academicYearId;
  private final boolean complete;
  private final BigDecimal finalGrade;
  private final int credits;
  private final int earnedCredits;
}
