package com.example.demo.endpoint.rest.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TranscriptResponse {

  private final UUID studentId;
  private final boolean complete;
  private final BigDecimal annualAverage;
  private final int earnedCredits;
  private final List<TranscriptCourseResponse> courses;
}
