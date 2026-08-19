package com.example.demo.endpoint.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentAcademicYearResultResponse {

  private final UUID academicYearId;
  private final String academicYearLabel;
  private final BigDecimal average;
  private final int earnedCredits;
  private final boolean complete;
}
