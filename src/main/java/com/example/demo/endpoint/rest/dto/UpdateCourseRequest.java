package com.example.demo.endpoint.rest.dto;

import jakarta.validation.constraints.Min;

public record UpdateCourseRequest(String ref, String title, @Min(1) Integer credits) {}
