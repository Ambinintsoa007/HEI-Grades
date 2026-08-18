package com.example.demo.endpoint.rest.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateCourseOfferingRequest(
    @NotNull UUID courseId, @NotNull UUID academicYearId, @NotNull UUID groupId) {}
