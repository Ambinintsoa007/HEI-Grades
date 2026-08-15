package com.example.demo.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GradeHistory {
  private final UUID id;
  private final UUID gradeId;
  private final BigDecimal oldScore;
  private final BigDecimal newScore;
  private final String reason;
  private final UUID changedBy;
  private final Instant changedAt;
}
