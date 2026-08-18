package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.CreateExamRequest;
import com.example.demo.endpoint.rest.dto.ExamResponse;
import com.example.demo.model.UserRole;
import com.example.demo.service.CourseOfferingAuthorizationService;
import com.example.demo.service.ExamService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ExamController {

  private final ExamService examService;
  private final CourseOfferingAuthorizationService courseOfferingAuthorizationService;

  @GetMapping("/course-offerings/{courseOfferingId}/exams")
  public List<ExamResponse> getExams(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID courseOfferingId) {
    authorize(jwt, courseOfferingId);
    return examService.listExams(courseOfferingId).stream().map(ExamResponse::from).toList();
  }

  @PostMapping("/course-offerings/{courseOfferingId}/exams")
  public ResponseEntity<ExamResponse> createExam(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID courseOfferingId,
      @Valid @RequestBody CreateExamRequest request) {
    authorize(jwt, courseOfferingId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ExamResponse.from(examService.createExam(courseOfferingId, request)));
  }

  private void authorize(Jwt jwt, UUID courseOfferingId) {
    UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
    UserRole role = UserRole.valueOf(jwt.getClaimAsString("role"));
    courseOfferingAuthorizationService.checkCanAccessCourseOffering(
        authenticatedUserId, role, courseOfferingId);
  }
}
