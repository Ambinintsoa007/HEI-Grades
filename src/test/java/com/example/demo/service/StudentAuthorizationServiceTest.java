package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.demo.model.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class StudentAuthorizationServiceTest {

  private final StudentAuthorizationService service = new StudentAuthorizationService();

  @Test
  void studentShouldAccessOwnData() {
    UUID studentId = UUID.randomUUID();

    assertDoesNotThrow(() -> service.checkCanAccessStudent(studentId, UserRole.STUDENT, studentId));
  }

  @Test
  void studentShouldNotAccessAnotherStudentData() {
    UUID authenticatedStudentId = UUID.randomUUID();
    UUID otherStudentId = UUID.randomUUID();

    assertThrows(
        AccessDeniedException.class,
        () ->
            service.checkCanAccessStudent(
                authenticatedStudentId, UserRole.STUDENT, otherStudentId));
  }

  @Test
  void adminShouldAccessStudentData() {
    assertDoesNotThrow(
        () -> service.checkCanAccessStudent(UUID.randomUUID(), UserRole.ADMIN, UUID.randomUUID()));
  }
}
