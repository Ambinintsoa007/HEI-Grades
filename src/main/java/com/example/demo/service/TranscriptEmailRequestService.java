package com.example.demo.service;

import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.endpoint.event.model.TranscriptEmailRequested;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.model.UserRole;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.UserRoleEntity;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TranscriptEmailRequestService {

  private final UserRepository userRepository;
  private final EventProducer<TranscriptEmailRequested> eventProducer;

  public void request(UUID authenticatedUserId, UserRole role, UUID studentId) {
    var student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

    if (student.getRole() != UserRoleEntity.STUDENT) {
      throw new ResourceNotFoundException("Student not found");
    }

    if (role != UserRole.STUDENT && role != UserRole.ADMIN) {
      throw new AccessDeniedException("Access denied");
    }

    if (role == UserRole.STUDENT && !authenticatedUserId.equals(studentId)) {
      throw new AccessDeniedException("Access denied");
    }

    var event = TranscriptEmailRequested.builder().studentId(studentId).build();
    eventProducer.accept(List.of(event));
  }
}
