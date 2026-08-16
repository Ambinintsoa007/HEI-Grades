package com.example.demo.endpoint.rest.dto;

import com.example.demo.model.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

  private final String token;
  private final UserRole role;
}
