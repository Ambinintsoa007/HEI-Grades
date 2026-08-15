package com.example.demo.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {

  private final UUID id;
  private final String firstName;
  private final String lastName;
  private final String email;
  private final String passwordHash;
  private final UserRole role;
  private final UserStatus status;

  // STUDENT only
  private final String std;
  private final UUID promotionId;
}
