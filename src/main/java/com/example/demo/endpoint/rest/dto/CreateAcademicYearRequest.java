package com.example.demo.endpoint.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAcademicYearRequest {

  @NotBlank private String label;
  @NotNull private LocalDate startDate;
  @NotNull private LocalDate endDate;
}
