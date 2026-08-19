package com.example.demo.endpoint.rest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveGradeRequest {

  @NotNull private UUID examId;

  @NotNull private UUID studentCourseEnrollmentId;

  @NotNull
  @DecimalMin("0.0")
  @DecimalMax("20.0")
  private BigDecimal score;

  private String correctionReason;
}
