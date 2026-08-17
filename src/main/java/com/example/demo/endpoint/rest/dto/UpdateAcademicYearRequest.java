package com.example.demo.endpoint.rest.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAcademicYearRequest {

  private String label;
  private LocalDate startDate;
  private LocalDate endDate;
}
