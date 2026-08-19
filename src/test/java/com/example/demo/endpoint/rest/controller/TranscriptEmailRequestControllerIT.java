package com.example.demo.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.demo.CourseManagementTestBase;
import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.endpoint.event.model.TranscriptEmailRequested;
import com.example.demo.repository.model.UserEntity;
import java.util.Collection;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class TranscriptEmailRequestControllerIT extends CourseManagementTestBase {

  @Autowired private TestRestTemplate restTemplate;
  @MockBean private EventProducer<TranscriptEmailRequested> eventProducer;
  @Captor private ArgumentCaptor<Collection<TranscriptEmailRequested>> eventsCaptor;

  private UserEntity student;
  private String studentUrl;

  @BeforeEach
  void setUp() {
    student = saveStudent();
    studentUrl = "/students/" + student.getId() + "/transcript/email";
  }

  @Test
  void studentCanRequestOwnTranscript() {
    ResponseEntity<Void> response =
        restTemplate.exchange(
            studentUrl, HttpMethod.POST, new HttpEntity<>(authHeaders(student)), Void.class);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    assertNull(response.getBody());
    verify(eventProducer).accept(eventsCaptor.capture());
    var events = eventsCaptor.getValue();
    assertEquals(1, events.size());
    assertEquals(student.getId(), events.iterator().next().getStudentId());
  }

  @Test
  void adminCanRequestAnyStudentTranscript() {
    ResponseEntity<Void> response =
        restTemplate.exchange(
            studentUrl, HttpMethod.POST, new HttpEntity<>(authHeaders(saveAdmin())), Void.class);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  void studentRequestingAnotherStudentIsForbidden() {
    UserEntity otherStudent = saveStudent();
    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/students/" + otherStudent.getId() + "/transcript/email",
            HttpMethod.POST,
            new HttpEntity<>(authHeaders(student)),
            Void.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verifyNoInteractions(eventProducer);
  }

  @Test
  void teacherRequestingTranscriptIsForbidden() {
    ResponseEntity<Void> response =
        restTemplate.exchange(
            studentUrl, HttpMethod.POST, new HttpEntity<>(authHeaders(saveTeacher())), Void.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verifyNoInteractions(eventProducer);
  }

  @Test
  void unknownStudentReturns404() {
    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/students/" + UUID.randomUUID() + "/transcript/email",
            HttpMethod.POST,
            new HttpEntity<>(authHeaders(saveAdmin())),
            Void.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verifyNoInteractions(eventProducer);
  }

  @Test
  void nonStudentTargetReturns404() {
    UserEntity teacher = saveTeacher();
    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/students/" + teacher.getId() + "/transcript/email",
            HttpMethod.POST,
            new HttpEntity<>(authHeaders(saveAdmin())),
            Void.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verifyNoInteractions(eventProducer);
  }
}
