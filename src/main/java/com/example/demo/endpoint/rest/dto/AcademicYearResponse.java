package com.example.demo.endpoint.rest.dto;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AcademicYearResponse {

  private final UUID id;
  private final String label;
  private final LocalDate startDate;
  private final LocalDate endDate;
}
