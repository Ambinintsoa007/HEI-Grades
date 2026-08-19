package com.example.demo.service.event;

import com.example.demo.endpoint.event.model.TranscriptEmailRequested;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.UserRoleEntity;
import com.example.demo.service.TranscriptEmailService;
import com.example.demo.service.TranscriptPdfService;
import com.example.demo.service.TranscriptService;
import com.example.demo.service.TranscriptStorageService;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TranscriptEmailRequestedService implements Consumer<TranscriptEmailRequested> {

  private final UserRepository userRepository;
  private final TranscriptService transcriptService;
  private final TranscriptPdfService transcriptPdfService;
  private final TranscriptStorageService transcriptStorageService;
  private final TranscriptEmailService transcriptEmailService;

  @Override
  public void accept(TranscriptEmailRequested event) {
    var studentId = event.getStudentId();

    var student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

    if (student.getRole() != UserRoleEntity.STUDENT) {
      throw new ResourceNotFoundException("Student not found");
    }

    var transcript = transcriptService.getTranscript(studentId);

    byte[] pdf = transcriptPdfService.generate(transcript);

    var transcriptUrl = transcriptStorageService.store(studentId, pdf);

    transcriptEmailService.send(student.getEmail(), transcriptUrl);
  }
}
