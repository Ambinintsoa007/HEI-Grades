package com.example.demo.endpoint.rest.dto;

import com.example.demo.model.Course;
import java.util.UUID;

public record CourseResponse(UUID id, String ref, String title, int credits) {

  public static CourseResponse from(Course course) {
    return new CourseResponse(
        course.getId(), course.getRef(), course.getTitle(), course.getCredits());
  }
}
