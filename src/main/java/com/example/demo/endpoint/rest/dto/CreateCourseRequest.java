package com.example.demo.endpoint.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCourseRequest(
    @NotBlank String ref, @NotBlank String title, @NotNull @Min(1) Integer credits) {}
