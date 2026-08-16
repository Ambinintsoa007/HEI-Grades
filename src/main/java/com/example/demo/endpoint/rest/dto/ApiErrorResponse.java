package com.example.demo.endpoint.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiErrorResponse {

  private final int status;
  private final String message;
}
