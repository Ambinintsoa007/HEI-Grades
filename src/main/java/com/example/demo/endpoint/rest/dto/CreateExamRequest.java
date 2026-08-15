package com.example.demo.endpoint.rest.dto;

import java.math.BigDecimal;

public record CreateExamRequest(String ref, BigDecimal coefficient) {}
