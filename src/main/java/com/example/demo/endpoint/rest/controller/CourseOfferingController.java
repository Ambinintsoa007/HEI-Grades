package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.AssignTeacherRequest;
import com.example.demo.endpoint.rest.dto.CourseOfferingResponse;
import com.example.demo.endpoint.rest.dto.CreateCourseOfferingRequest;
import com.example.demo.service.CourseOfferingService;
import com.example.demo.service.TeacherAssignmentService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class CourseOfferingController extends ApiExceptionHandler {

  private final CourseOfferingService courseOfferingService;
  private final TeacherAssignmentService teacherAssignmentService;

  @GetMapping("/course-offerings")
  public List<CourseOfferingResponse> getCourseOfferings(
      @RequestParam(required = false) UUID academicYearId,
      @RequestParam(required = false) UUID groupId) {
    return courseOfferingService.listCourseOfferings(academicYearId, groupId).stream()
        .map(CourseOfferingResponse::from)
        .toList();
  }

  @PostMapping("/course-offerings")
  public ResponseEntity<CourseOfferingResponse> createCourseOffering(
      @RequestBody CreateCourseOfferingRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(CourseOfferingResponse.from(courseOfferingService.createCourseOffering(request)));
  }

  @PostMapping("/course-offerings/{courseOfferingId}/teachers")
  public ResponseEntity<Void> assignTeacher(
      @PathVariable UUID courseOfferingId, @RequestBody AssignTeacherRequest request) {
    teacherAssignmentService.assignTeacher(courseOfferingId, request.teacherId());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
