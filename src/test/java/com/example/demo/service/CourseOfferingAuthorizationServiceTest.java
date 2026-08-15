package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.repository.CourseOfferingTeacherRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseOfferingAuthorizationServiceTest {

  @Test
  void isTeacherAssigned_true_when_assignment_exists() {
    var repository = mock(CourseOfferingTeacherRepository.class);
    var service = new CourseOfferingAuthorizationService(repository);
    var teacherId = UUID.randomUUID();
    var offeringId = UUID.randomUUID();
    when(repository.existsByCourseOffering_IdAndTeacher_Id(offeringId, teacherId)).thenReturn(true);

    var result = service.isTeacherAssigned(teacherId, offeringId);

    assertTrue(result);
    verify(repository).existsByCourseOffering_IdAndTeacher_Id(offeringId, teacherId);
  }

  @Test
  void isTeacherAssigned_false_when_no_assignment() {
    var repository = mock(CourseOfferingTeacherRepository.class);
    var service = new CourseOfferingAuthorizationService(repository);
    var teacherId = UUID.randomUUID();
    var offeringId = UUID.randomUUID();
    when(repository.existsByCourseOffering_IdAndTeacher_Id(offeringId, teacherId))
        .thenReturn(false);

    var result = service.isTeacherAssigned(teacherId, offeringId);

    assertFalse(result);
  }
}
