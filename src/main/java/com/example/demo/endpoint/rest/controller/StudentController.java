package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.StudentCourseResponse;
import com.example.demo.endpoint.rest.dto.StudentGroupAssignmentRequest;
import com.example.demo.endpoint.rest.dto.UpdateStudentRequest;
import com.example.demo.endpoint.rest.dto.UserResponse;
import com.example.demo.model.UserRole;
import com.example.demo.service.StudentAuthorizationService;
import com.example.demo.service.StudentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

  private final StudentService studentService;

  private final StudentAuthorizationService studentAuthorizationService;

  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentStudent(@AuthenticationPrincipal Jwt jwt) {

    return ResponseEntity.ok(studentService.getStudent(UUID.fromString(jwt.getSubject())));
  }

  @PatchMapping("/{studentId}")
  public ResponseEntity<UserResponse> updateStudent(
      @PathVariable UUID studentId, @Valid @RequestBody UpdateStudentRequest request) {

    return ResponseEntity.ok(studentService.update(studentId, request));
  }

  @PostMapping("/{studentId}/group-assignments")
  public ResponseEntity<Void> assignGroup(
      @PathVariable UUID studentId, @Valid @RequestBody StudentGroupAssignmentRequest request) {

    studentService.assignGroup(studentId, request);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/{studentId}/courses")
  public ResponseEntity<List<StudentCourseResponse>> getStudentCourses(
      @PathVariable UUID studentId, @AuthenticationPrincipal Jwt jwt) {

    UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
    UserRole role = UserRole.valueOf(jwt.getClaimAsString("role"));

    studentAuthorizationService.checkCanAccessStudent(authenticatedUserId, role, studentId);

    return ResponseEntity.ok(studentService.getCourses(studentId));
  }
}
