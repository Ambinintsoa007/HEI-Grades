package com.example.demo.endpoint.rest.dto;

import java.util.UUID;

public record CreateCourseOfferingRequest(UUID courseId, UUID academicYearId, UUID groupId) {}
