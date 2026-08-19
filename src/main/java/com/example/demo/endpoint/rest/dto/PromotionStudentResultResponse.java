package com.example.demo.endpoint.rest.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PromotionStudentResultResponse {

  private final UUID studentId;
  private final String std;
  private final String firstName;
  private final String lastName;
  private final boolean complete;
  private final boolean graduate;
  private final int earnedCredits;
  private final int totalCredits;
  private final List<AcademicYearResultResponse> academicYears;
}
