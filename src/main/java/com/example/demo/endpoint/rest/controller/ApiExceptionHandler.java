package com.example.demo.endpoint.rest.controller;

import com.example.demo.service.exception.BusinessException;
import com.example.demo.service.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public abstract class ApiExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Void> handleNotFound(ResourceNotFoundException e) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Void> handleBadRequest(BusinessException e) {
    return ResponseEntity.badRequest().build();
  }
}
