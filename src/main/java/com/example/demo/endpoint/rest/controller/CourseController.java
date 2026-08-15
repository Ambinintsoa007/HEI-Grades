package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.CourseResponse;
import com.example.demo.endpoint.rest.dto.CreateCourseRequest;
import com.example.demo.endpoint.rest.dto.UpdateCourseRequest;
import com.example.demo.service.CourseService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class CourseController extends ApiExceptionHandler {

  private final CourseService courseService;

  @GetMapping("/courses")
  public List<CourseResponse> getCourses() {
    return courseService.listCourses().stream().map(CourseResponse::from).toList();
  }

  @PostMapping("/courses")
  public ResponseEntity<CourseResponse> createCourse(@RequestBody CreateCourseRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(CourseResponse.from(courseService.createCourse(request)));
  }

  @PatchMapping("/courses/{courseId}")
  public CourseResponse updateCourse(
      @PathVariable UUID courseId, @RequestBody UpdateCourseRequest request) {
    return CourseResponse.from(courseService.updateCourse(courseId, request));
  }
}
