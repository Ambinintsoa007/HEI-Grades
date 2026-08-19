package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.GradeHistoryResponse;
import com.example.demo.endpoint.rest.dto.GradeResponse;
import com.example.demo.endpoint.rest.dto.SaveGradeRequest;
import com.example.demo.model.UserRole;
import com.example.demo.service.GradeService;
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
@RequiredArgsConstructor
public class GradeController {

  private final GradeService gradeService;

  @PostMapping("/grades")
  public ResponseEntity<GradeResponse> saveGrade(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SaveGradeRequest request) {

    UUID userId = UUID.fromString(jwt.getSubject());
    UserRole role = UserRole.valueOf(jwt.getClaimAsString("role"));

    var result = gradeService.save(userId, role, request);

    return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
        .body(result.grade());
  }

  @GetMapping("/grades/{gradeId}/history")
  public List<GradeHistoryResponse> getGradeHistory(
      @PathVariable UUID gradeId, @AuthenticationPrincipal Jwt jwt) {

    UUID userId = UUID.fromString(jwt.getSubject());
    UserRole role = UserRole.valueOf(jwt.getClaimAsString("role"));

    return gradeService.getHistory(userId, role, gradeId);
  }

  @GetMapping("/exams/{examId}/grades")
  public List<GradeResponse> getExamGrades(
      @PathVariable UUID examId, @AuthenticationPrincipal Jwt jwt) {

    UUID userId = UUID.fromString(jwt.getSubject());
    UserRole role = UserRole.valueOf(jwt.getClaimAsString("role"));

    return gradeService.getExamGrades(userId, role, examId);
  }
}
