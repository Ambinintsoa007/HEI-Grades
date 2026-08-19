package com.example.demo.endpoint.event.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class TranscriptEmailRequestedTest {

  @Test
  void shouldContainRequestedStudentId() {
    UUID studentId = UUID.randomUUID();

    var event = TranscriptEmailRequested.builder().studentId(studentId).build();

    assertEquals(studentId, event.getStudentId());
    assertNotNull(event.maxConsumerDuration());
    assertNotNull(event.maxConsumerBackoffBetweenRetries());
  }
}
