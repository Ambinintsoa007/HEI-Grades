package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.model.UserRole;
import com.example.demo.repository.CourseOfferingTeacherRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class CourseOfferingAuthorizationServiceTest {

  private final CourseOfferingTeacherRepository repository =
      mock(CourseOfferingTeacherRepository.class);
  private final CourseOfferingAuthorizationService service =
      new CourseOfferingAuthorizationService(repository);

  @Test
  void admin_can_access_any_offering() {
    service.checkCanAccessCourseOffering(UUID.randomUUID(), UserRole.ADMIN, UUID.randomUUID());

    verify(repository, never())
        .existsByCourseOffering_IdAndTeacher_Id(
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  void assigned_teacher_can_access_offering() {
    var teacherId = UUID.randomUUID();
    var offeringId = UUID.randomUUID();
    when(repository.existsByCourseOffering_IdAndTeacher_Id(offeringId, teacherId)).thenReturn(true);

    service.checkCanAccessCourseOffering(teacherId, UserRole.TEACHER, offeringId);

    verify(repository).existsByCourseOffering_IdAndTeacher_Id(offeringId, teacherId);
  }

  @Test
  void unassigned_teacher_is_denied() {
    var teacherId = UUID.randomUUID();
    var offeringId = UUID.randomUUID();
    when(repository.existsByCourseOffering_IdAndTeacher_Id(offeringId, teacherId))
        .thenReturn(false);

    assertThrows(
        AccessDeniedException.class,
        () -> service.checkCanAccessCourseOffering(teacherId, UserRole.TEACHER, offeringId));
  }

  @Test
  void student_is_denied() {
    assertThrows(
        AccessDeniedException.class,
        () ->
            service.checkCanAccessCourseOffering(
                UUID.randomUUID(), UserRole.STUDENT, UUID.randomUUID()));
  }

  @Test
  void isTeacherAssigned_true_when_assignment_exists() {
    var teacherId = UUID.randomUUID();
    var offeringId = UUID.randomUUID();
    when(repository.existsByCourseOffering_IdAndTeacher_Id(offeringId, teacherId)).thenReturn(true);

    var result = service.isTeacherAssigned(teacherId, offeringId);

    assertTrue(result);
    verify(repository).existsByCourseOffering_IdAndTeacher_Id(offeringId, teacherId);
  }

  @Test
  void isTeacherAssigned_false_when_no_assignment() {
    var teacherId = UUID.randomUUID();
    var offeringId = UUID.randomUUID();
    when(repository.existsByCourseOffering_IdAndTeacher_Id(offeringId, teacherId))
        .thenReturn(false);

    var result = service.isTeacherAssigned(teacherId, offeringId);

    assertFalse(result);
  }
}
