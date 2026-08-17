package com.example.demo.endpoint.rest.exception;

public class ResourceNotFoundException extends BusinessException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
