package com.example.demo.endpoint.rest.dto;

import com.example.demo.model.Pathway;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GraduateResponse {

  private final UUID studentId;
  private final String std;
  private final String firstName;
  private final String lastName;
  private final Pathway pathway;
  private final BigDecimal overallAverage;
}
