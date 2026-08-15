package com.example.demo.model;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AcademicYear {

  private final UUID id;
  private final String label;
  private final LocalDate startDate;
  private final LocalDate endDate;
}
