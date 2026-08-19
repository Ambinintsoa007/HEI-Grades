package com.example.demo.endpoint.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentGradeResponse {

  private final UUID gradeId;
  private final UUID examId;
  private final String examRef;
  private final UUID courseId;
  private final String courseRef;
  private final BigDecimal score;
  private final BigDecimal coefficient;
}
