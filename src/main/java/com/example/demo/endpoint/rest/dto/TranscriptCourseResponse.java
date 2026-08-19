package com.example.demo.endpoint.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TranscriptCourseResponse {

  private final UUID courseId;
  private final String ref;
  private final String title;
  private final int credits;
  private final BigDecimal finalGrade;
  private final Boolean validated;
}
