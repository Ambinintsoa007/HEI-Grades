package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.model.StudentCourseResult;
import com.example.demo.model.UserRole;
import com.example.demo.repository.StudentCourseEnrollmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.CourseEntity;
import com.example.demo.repository.model.StudentCourseEnrollmentEntity;
import com.example.demo.repository.model.UserEntity;
import com.example.demo.repository.model.UserRoleEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class TranscriptServiceTest {

  private UserRepository userRepository;
  private StudentCourseEnrollmentRepository enrollmentRepository;
  private StudentCourseResultService resultService;

  private TranscriptService transcriptService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    enrollmentRepository = mock(StudentCourseEnrollmentRepository.class);
    resultService = mock(StudentCourseResultService.class);

    transcriptService = new TranscriptService(userRepository, enrollmentRepository, resultService);
  }

  @Test
  void adminShouldGetCompleteTranscriptWithWeightedAverage() {
    UUID adminId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();

    UserEntity student = mock(UserEntity.class);
    when(student.getRole()).thenReturn(UserRoleEntity.STUDENT);

    CourseEntity course1 =
        CourseEntity.builder()
            .id(UUID.randomUUID())
            .ref("PROG4")
            .title("Programming")
            .credits(4)
            .build();

    CourseEntity course2 =
        CourseEntity.builder()
            .id(UUID.randomUUID())
            .ref("SYS1")
            .title("Systems")
            .credits(6)
            .build();

    StudentCourseEnrollmentEntity enrollment1 =
        StudentCourseEnrollmentEntity.builder().id(UUID.randomUUID()).course(course1).build();

    StudentCourseEnrollmentEntity enrollment2 =
        StudentCourseEnrollmentEntity.builder().id(UUID.randomUUID()).course(course2).build();

    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

    when(enrollmentRepository.findAllByStudent_Id(studentId))
        .thenReturn(List.of(enrollment1, enrollment2));

    when(resultService.calculate(enrollment1.getId()))
        .thenReturn(
            StudentCourseResult.builder()
                .enrollmentId(enrollment1.getId())
                .courseId(course1.getId())
                .complete(true)
                .finalGrade(new BigDecimal("12"))
                .credits(4)
                .earnedCredits(4)
                .build());

    when(resultService.calculate(enrollment2.getId()))
        .thenReturn(
            StudentCourseResult.builder()
                .enrollmentId(enrollment2.getId())
                .courseId(course2.getId())
                .complete(true)
                .finalGrade(new BigDecimal("16"))
                .credits(6)
                .earnedCredits(6)
                .build());

    var result = transcriptService.getTranscript(adminId, UserRole.ADMIN, studentId);

    assertTrue(result.isComplete());

    // (12*4 + 16*6) / 10 = 14.40
    assertEquals(new BigDecimal("14.40"), result.getAnnualAverage());

    assertEquals(10, result.getEarnedCredits());
    assertEquals(2, result.getCourses().size());

    assertTrue(result.getCourses().get(0).getValidated());
    assertTrue(result.getCourses().get(1).getValidated());
  }

  @Test
  void studentShouldAccessOwnTranscript() {
    UUID studentId = UUID.randomUUID();

    UserEntity student = mock(UserEntity.class);
    when(student.getRole()).thenReturn(UserRoleEntity.STUDENT);

    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

    when(enrollmentRepository.findAllByStudent_Id(studentId)).thenReturn(List.of());

    assertDoesNotThrow(
        () -> transcriptService.getTranscript(studentId, UserRole.STUDENT, studentId));
  }

  @Test
  void studentShouldNotAccessAnotherStudentTranscript() {
    UUID authenticatedStudentId = UUID.randomUUID();
    UUID otherStudentId = UUID.randomUUID();

    UserEntity student = mock(UserEntity.class);
    when(student.getRole()).thenReturn(UserRoleEntity.STUDENT);

    when(userRepository.findById(otherStudentId)).thenReturn(Optional.of(student));

    assertThrows(
        AccessDeniedException.class,
        () ->
            transcriptService.getTranscript(
                authenticatedStudentId, UserRole.STUDENT, otherStudentId));
  }

  @Test
  void teacherShouldNotAccessStudentTranscript() {
    UUID teacherId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();

    UserEntity student = mock(UserEntity.class);
    when(student.getRole()).thenReturn(UserRoleEntity.STUDENT);

    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

    assertThrows(
        AccessDeniedException.class,
        () -> transcriptService.getTranscript(teacherId, UserRole.TEACHER, studentId));
  }

  @Test
  void shouldRejectUnknownStudent() {
    UUID studentId = UUID.randomUUID();

    when(userRepository.findById(studentId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> transcriptService.getTranscript(UUID.randomUUID(), UserRole.ADMIN, studentId));
  }

  @Test
  void incompleteCourseShouldMakeTranscriptIncomplete() {
    UUID studentId = UUID.randomUUID();

    UserEntity student = mock(UserEntity.class);
    when(student.getRole()).thenReturn(UserRoleEntity.STUDENT);

    CourseEntity course =
        CourseEntity.builder()
            .id(UUID.randomUUID())
            .ref("PROG4")
            .title("Programming")
            .credits(5)
            .build();

    StudentCourseEnrollmentEntity enrollment =
        StudentCourseEnrollmentEntity.builder().id(UUID.randomUUID()).course(course).build();

    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

    when(enrollmentRepository.findAllByStudent_Id(studentId)).thenReturn(List.of(enrollment));

    when(resultService.calculate(enrollment.getId()))
        .thenReturn(
            StudentCourseResult.builder()
                .enrollmentId(enrollment.getId())
                .courseId(course.getId())
                .complete(false)
                .finalGrade(null)
                .credits(5)
                .earnedCredits(0)
                .build());

    var result = transcriptService.getTranscript(studentId, UserRole.STUDENT, studentId);

    assertFalse(result.isComplete());
    assertNull(result.getAnnualAverage());
    assertEquals(0, result.getEarnedCredits());

    var transcriptCourse = result.getCourses().getFirst();

    assertNull(transcriptCourse.getFinalGrade());
    assertNull(transcriptCourse.getValidated());
  }

  @Test
  void failedCourseShouldNotEarnCredits() {
    UUID studentId = UUID.randomUUID();

    UserEntity student = mock(UserEntity.class);
    when(student.getRole()).thenReturn(UserRoleEntity.STUDENT);

    CourseEntity course =
        CourseEntity.builder()
            .id(UUID.randomUUID())
            .ref("MATH1")
            .title("Mathematics")
            .credits(6)
            .build();

    StudentCourseEnrollmentEntity enrollment =
        StudentCourseEnrollmentEntity.builder().id(UUID.randomUUID()).course(course).build();

    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

    when(enrollmentRepository.findAllByStudent_Id(studentId)).thenReturn(List.of(enrollment));

    when(resultService.calculate(enrollment.getId()))
        .thenReturn(
            StudentCourseResult.builder()
                .enrollmentId(enrollment.getId())
                .courseId(course.getId())
                .complete(true)
                .finalGrade(new BigDecimal("8"))
                .credits(6)
                .earnedCredits(0)
                .build());

    var result = transcriptService.getTranscript(studentId, UserRole.STUDENT, studentId);

    assertTrue(result.isComplete());
    assertEquals(new BigDecimal("8.00"), result.getAnnualAverage());
    assertEquals(0, result.getEarnedCredits());
    assertFalse(result.getCourses().getFirst().getValidated());
  }

  @Test
  void workerShouldGetTranscriptWithoutAuthenticationContext() {
    UUID studentId = UUID.randomUUID();

    UserEntity student = mock(UserEntity.class);

    when(student.getRole()).thenReturn(UserRoleEntity.STUDENT);

    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

    when(enrollmentRepository.findAllByStudent_Id(studentId)).thenReturn(List.of());

    var result = transcriptService.getTranscript(studentId);

    assertEquals(studentId, result.getStudentId());
    assertTrue(result.getCourses().isEmpty());
  }
}
