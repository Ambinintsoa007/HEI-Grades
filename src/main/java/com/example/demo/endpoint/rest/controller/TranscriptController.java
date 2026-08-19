package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.TranscriptResponse;
import com.example.demo.model.UserRole;
import com.example.demo.service.TranscriptService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TranscriptController {

  private final TranscriptService transcriptService;

  @GetMapping("/students/{studentId}/transcript")
  public TranscriptResponse getStudentTranscript(
      @PathVariable UUID studentId, @AuthenticationPrincipal Jwt jwt) {

    UUID authenticatedUserId = UUID.fromString(jwt.getSubject());

    UserRole role = UserRole.valueOf(jwt.getClaimAsString("role"));

    return transcriptService.getTranscript(authenticatedUserId, role, studentId);
  }
}
