package com.example.demo.model;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseGradeResult {
  private final boolean complete;
  private final BigDecimal finalGrade;
}
