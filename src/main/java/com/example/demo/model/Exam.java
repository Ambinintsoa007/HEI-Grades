package com.example.demo.model;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Exam {
  private final UUID id;
  private final String ref;
  private final UUID courseOfferingId;
  private final BigDecimal coefficient;
}
