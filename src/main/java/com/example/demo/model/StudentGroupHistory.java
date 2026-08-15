package com.example.demo.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentGroupHistory {

  private final UUID id;
  private final UUID studentId;
  private final UUID academicYearId;
  private final UUID groupId;
  private final Pathway pathway;
  private final Instant startedAt;
  private final Instant endedAt;
}
