package com.example.demo.endpoint.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateExamRequest(
    @NotBlank String ref,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal coefficient) {}
