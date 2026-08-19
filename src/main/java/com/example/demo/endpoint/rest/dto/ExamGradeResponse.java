package com.example.demo.endpoint.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExamGradeResponse {

  private final UUID gradeId;
  private final UUID studentId;
  private final UUID studentCourseEnrollmentId;
  private final String std;
  private final String firstName;
  private final String lastName;
  private final BigDecimal score;
}
