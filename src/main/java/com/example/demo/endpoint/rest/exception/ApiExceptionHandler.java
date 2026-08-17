package com.example.demo.endpoint.rest.exception;

import com.example.demo.endpoint.rest.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage()));
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException exception) {

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ApiErrorResponse(HttpStatus.CONFLICT.value(), exception.getMessage()));
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException exception) {

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage()));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
      AuthenticationException exception) {

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ApiErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationException(
      MethodArgumentNotValidException exception) {

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid request"));
  }
}
