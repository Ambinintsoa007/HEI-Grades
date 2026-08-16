package com.example.demo.endpoint.rest.exception;

import com.example.demo.endpoint.rest.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
      AuthenticationException exception) {

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ApiErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials"));
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception) {
    return ResponseEntity.status(exception.getStatus())
        .body(new ApiErrorResponse(exception.getStatus().value(), exception.getMessage()));
  }
}
