package com.example.demo.model;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExamGrade {
  private final BigDecimal coefficient;
  private final BigDecimal score;
}
