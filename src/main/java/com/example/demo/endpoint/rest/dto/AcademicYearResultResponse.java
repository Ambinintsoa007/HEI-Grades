package com.example.demo.endpoint.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AcademicYearResultResponse {

  private final UUID academicYearId;
  private final String label;
  private final BigDecimal average;
  private final int earnedCredits;
  private final int totalCredits;
  private final boolean complete;
}
