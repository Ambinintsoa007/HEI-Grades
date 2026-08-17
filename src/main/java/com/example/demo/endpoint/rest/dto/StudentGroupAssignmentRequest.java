package com.example.demo.endpoint.rest.dto;

import com.example.demo.model.Pathway;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentGroupAssignmentRequest {

  @NotNull private UUID academicYearId;

  @NotNull private UUID groupId;

  private Pathway pathway;

  @NotNull private Instant startedAt;
}
