package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.endpoint.event.model.TranscriptEmailRequested;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.model.UserRole;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.UserEntity;
import com.example.demo.repository.model.UserRoleEntity;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

class TranscriptEmailRequestServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private EventProducer<TranscriptEmailRequested> eventProducer;
  @Captor private ArgumentCaptor<Collection<TranscriptEmailRequested>> eventsCaptor;

  private TranscriptEmailRequestService transcriptEmailRequestService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    transcriptEmailRequestService =
        new TranscriptEmailRequestService(userRepository, eventProducer);
  }

  @Test
  void studentRequestsOwnTranscriptPublishesEvent() {
    UUID studentId = UUID.randomUUID();
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student(studentId)));

    transcriptEmailRequestService.request(studentId, UserRole.STUDENT, studentId);

    verify(eventProducer, times(1)).accept(eventsCaptor.capture());
    var events = eventsCaptor.getValue();
    assertEquals(1, events.size());
    assertEquals(studentId, events.iterator().next().getStudentId());
  }

  @Test
  void adminRequestsAnyStudentTranscriptPublishesEvent() {
    UUID adminId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student(studentId)));

    transcriptEmailRequestService.request(adminId, UserRole.ADMIN, studentId);

    verify(eventProducer, times(1)).accept(eventsCaptor.capture());
    var events = eventsCaptor.getValue();
    assertEquals(1, events.size());
    assertEquals(studentId, events.iterator().next().getStudentId());
  }

  @Test
  void studentRequestingAnotherStudentThrowsAccessDenied() {
    UUID authenticatedUserId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student(studentId)));

    assertThrows(
        AccessDeniedException.class,
        () ->
            transcriptEmailRequestService.request(
                authenticatedUserId, UserRole.STUDENT, studentId));
    verifyNoInteractions(eventProducer);
  }

  @Test
  void teacherRequestThrowsAccessDenied() {
    UUID teacherId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student(studentId)));

    assertThrows(
        AccessDeniedException.class,
        () -> transcriptEmailRequestService.request(teacherId, UserRole.TEACHER, studentId));
    verifyNoInteractions(eventProducer);
  }

  @Test
  void unknownStudentThrowsResourceNotFound() {
    UUID adminId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    when(userRepository.findById(studentId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> transcriptEmailRequestService.request(adminId, UserRole.ADMIN, studentId));
    verifyNoInteractions(eventProducer);
  }

  @Test
  void targetNotStudentThrowsResourceNotFound() {
    UUID adminId = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();
    when(userRepository.findById(targetId))
        .thenReturn(Optional.of(user(targetId, UserRoleEntity.TEACHER)));

    assertThrows(
        ResourceNotFoundException.class,
        () -> transcriptEmailRequestService.request(adminId, UserRole.ADMIN, targetId));
    verifyNoInteractions(eventProducer);
  }

  @Test
  void publishesOnlyOneEvent() {
    UUID studentId = UUID.randomUUID();
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student(studentId)));

    transcriptEmailRequestService.request(studentId, UserRole.STUDENT, studentId);

    verify(eventProducer, times(1)).accept(any());
  }

  private UserEntity student(UUID id) {
    return user(id, UserRoleEntity.STUDENT);
  }

  private UserEntity user(UUID id, UserRoleEntity role) {
    return UserEntity.builder()
        .id(id)
        .firstName("Jane")
        .lastName("Doe")
        .email("jane@hei.school")
        .passwordHash("hash")
        .role(role)
        .build();
  }
}
