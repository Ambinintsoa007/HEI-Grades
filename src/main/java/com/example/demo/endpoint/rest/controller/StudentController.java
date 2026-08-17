package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.UpdateStudentRequest;
import com.example.demo.endpoint.rest.dto.UserResponse;
import com.example.demo.service.StudentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

  private final StudentService studentService;

  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentStudent(@AuthenticationPrincipal Jwt jwt) {

    return ResponseEntity.ok(studentService.getStudent(UUID.fromString(jwt.getSubject())));
  }

  @PatchMapping("/{studentId}")
  public ResponseEntity<UserResponse> updateStudent(
      @PathVariable UUID studentId, @Valid @RequestBody UpdateStudentRequest request) {

    return ResponseEntity.ok(studentService.update(studentId, request));
  }
}
