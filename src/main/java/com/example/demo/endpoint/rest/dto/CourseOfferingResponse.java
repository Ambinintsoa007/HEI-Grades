package com.example.demo.endpoint.rest.dto;

import com.example.demo.model.CourseOffering;
import java.util.UUID;

public record CourseOfferingResponse(UUID id, UUID courseId, UUID academicYearId, UUID groupId) {

  public static CourseOfferingResponse from(CourseOffering offering) {
    return new CourseOfferingResponse(
        offering.getId(),
        offering.getCourseId(),
        offering.getAcademicYearId(),
        offering.getGroupId());
  }
}
