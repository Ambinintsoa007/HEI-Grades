package com.example.demo.service.event;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.example.demo.endpoint.event.model.TranscriptEmailRequested;
import com.example.demo.endpoint.rest.dto.TranscriptResponse;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.UserEntity;
import com.example.demo.repository.model.UserRoleEntity;
import com.example.demo.service.TranscriptEmailService;
import com.example.demo.service.TranscriptPdfService;
import com.example.demo.service.TranscriptService;
import com.example.demo.service.TranscriptStorageService;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TranscriptEmailRequestedServiceTest {

  private UserRepository userRepository;
  private TranscriptService transcriptService;
  private TranscriptPdfService transcriptPdfService;
  private TranscriptStorageService transcriptStorageService;
  private TranscriptEmailService transcriptEmailService;

  private TranscriptEmailRequestedService service;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    transcriptService = mock(TranscriptService.class);
    transcriptPdfService = mock(TranscriptPdfService.class);
    transcriptStorageService = mock(TranscriptStorageService.class);
    transcriptEmailService = mock(TranscriptEmailService.class);

    service =
        new TranscriptEmailRequestedService(
            userRepository,
            transcriptService,
            transcriptPdfService,
            transcriptStorageService,
            transcriptEmailService);
  }

  @Test
  void shouldGenerateStoreAndEmailTranscript() throws Exception {
    UUID studentId = UUID.randomUUID();

    var event = TranscriptEmailRequested.builder().studentId(studentId).build();

    var student =
        UserEntity.builder()
            .id(studentId)
            .email("student@example.com")
            .role(UserRoleEntity.STUDENT)
            .build();

    var transcript = mock(TranscriptResponse.class);

    byte[] pdf = "%PDF-test".getBytes();

    var url = URI.create("https://example.com/transcript.pdf").toURL();

    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

    when(transcriptService.getTranscript(studentId)).thenReturn(transcript);

    when(transcriptPdfService.generate(transcript)).thenReturn(pdf);

    when(transcriptStorageService.store(studentId, pdf)).thenReturn(url);

    service.accept(event);

    verify(userRepository).findById(studentId);
    verify(transcriptService).getTranscript(studentId);
    verify(transcriptPdfService).generate(transcript);
    verify(transcriptStorageService).store(studentId, pdf);

    verify(transcriptEmailService).send("student@example.com", url);
  }

  @Test
  void unknownStudentShouldFailWithoutStartingDelivery() {
    UUID studentId = UUID.randomUUID();

    var event = TranscriptEmailRequested.builder().studentId(studentId).build();

    when(userRepository.findById(studentId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> service.accept(event));

    verifyNoInteractions(
        transcriptService, transcriptPdfService, transcriptStorageService, transcriptEmailService);
  }

  @Test
  void nonStudentShouldFailWithoutStartingDelivery() {
    UUID userId = UUID.randomUUID();

    var event = TranscriptEmailRequested.builder().studentId(userId).build();

    var teacher =
        UserEntity.builder()
            .id(userId)
            .email("teacher@example.com")
            .role(UserRoleEntity.TEACHER)
            .build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(teacher));

    assertThrows(ResourceNotFoundException.class, () -> service.accept(event));

    verifyNoInteractions(
        transcriptService, transcriptPdfService, transcriptStorageService, transcriptEmailService);
  }
}
