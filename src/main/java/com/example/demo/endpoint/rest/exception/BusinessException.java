package com.example.demo.endpoint.rest.exception;

public class BusinessException extends RuntimeException {

  public BusinessException(String message) {
    super(message);
  }
}
