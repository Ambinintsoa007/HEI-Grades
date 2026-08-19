package com.example.demo.endpoint.rest.dto;

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
  private final String email;
  private final int earnedCredits;
}
