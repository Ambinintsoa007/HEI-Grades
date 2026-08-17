package com.example.demo.endpoint.rest.dto;

import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

  private final UUID id;
  private final String firstName;
  private final String lastName;
  private final String email;
  private final UserRole role;
  private final UserStatus status;
  private final String std;
  private final UUID promotionId;
}
