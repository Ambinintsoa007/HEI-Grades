package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.model.StudentCourseResult;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.StudentCourseEnrollmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.AcademicYearEntity;
import com.example.demo.repository.model.PromotionEntity;
import com.example.demo.repository.model.StudentCourseEnrollmentEntity;
import com.example.demo.repository.model.UserEntity;
import com.example.demo.repository.model.UserRoleEntity;
import com.example.demo.repository.model.UserStatusEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PromotionResultServiceTest {

  private PromotionRepository promotionRepository;
  private UserRepository userRepository;
  private StudentCourseEnrollmentRepository studentCourseEnrollmentRepository;
  private AcademicYearRepository academicYearRepository;
  private StudentCourseResultService studentCourseResultService;
  private PromotionResultService promotionResultService;

  private UUID promotionId;
  private UUID yearId;

  @BeforeEach
  void setUp() {
    promotionRepository = mock(PromotionRepository.class);
    userRepository = mock(UserRepository.class);
    studentCourseEnrollmentRepository = mock(StudentCourseEnrollmentRepository.class);
    academicYearRepository = mock(AcademicYearRepository.class);
    studentCourseResultService = mock(StudentCourseResultService.class);

    promotionResultService =
        new PromotionResultService(
            promotionRepository,
            userRepository,
            studentCourseEnrollmentRepository,
            academicYearRepository,
            studentCourseResultService);

    promotionId = UUID.randomUUID();
    yearId = UUID.randomUUID();
    when(promotionRepository.findById(promotionId))
        .thenReturn(Optional.of(PromotionEntity.builder().id(promotionId).name("Promo").build()));
    when(academicYearRepository.findById(yearId))
        .thenReturn(
            Optional.of(AcademicYearEntity.builder().id(yearId).label("2024-2025").build()));
  }

  @Test
  void promotionNotFoundThrowsResourceNotFoundException() {
    UUID missingId = UUID.randomUUID();
    when(promotionRepository.findById(missingId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> promotionResultService.getPromotionResults(missingId));
    assertThrows(
        ResourceNotFoundException.class, () -> promotionResultService.getGraduates(missingId));
    verify(userRepository, never()).findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, missingId);
  }

  @Test
  void allCoursesCompleteAndPassingMakesGraduate() {
    var student = student("STD-001");
    var enrollment = enrollment();
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of(student));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(student.getId()))
        .thenReturn(List.of(enrollment));
    when(studentCourseResultService.calculate(enrollment.getId()))
        .thenReturn(result(true, new BigDecimal("12.00"), 5, 5));

    var results = promotionResultService.getPromotionResults(promotionId);

    assertEquals(1, results.size());
    var result = results.get(0);
    assertTrue(result.isComplete());
    assertTrue(result.isGraduate());
    assertEquals(5, result.getEarnedCredits());
    assertEquals(5, result.getTotalCredits());
    assertEquals(1, result.getAcademicYears().size());
    var year = result.getAcademicYears().get(0);
    assertTrue(year.isComplete());
    assertEquals(new BigDecimal("12.00"), year.getAverage());
    assertEquals("2024-2025", year.getLabel());
    assertEquals(5, year.getEarnedCredits());
    assertEquals(5, year.getTotalCredits());
  }

  @Test
  void oneCourseBelowTenIsNotGraduateEvenWithAverageAboveTen() {
    var student = student("STD-002");
    var enrollmentA = enrollment();
    var enrollmentB = enrollment();
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of(student));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(student.getId()))
        .thenReturn(List.of(enrollmentA, enrollmentB));
    when(studentCourseResultService.calculate(enrollmentA.getId()))
        .thenReturn(result(true, new BigDecimal("12.00"), 5, 5));
    when(studentCourseResultService.calculate(enrollmentB.getId()))
        .thenReturn(result(true, new BigDecimal("8.00"), 5, 0));

    var results = promotionResultService.getPromotionResults(promotionId);

    assertEquals(1, results.size());
    var result = results.get(0);
    assertTrue(result.isComplete());
    assertFalse(result.isGraduate());
    assertEquals(5, result.getEarnedCredits());
    assertEquals(10, result.getTotalCredits());
    assertEquals(1, result.getAcademicYears().size());
    assertEquals(new BigDecimal("10.00"), result.getAcademicYears().get(0).getAverage());
  }

  @Test
  void missingGradeMakesCourseIncompleteAndStudentNotGraduate() {
    var student = student("STD-003");
    var enrollment = enrollment();
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of(student));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(student.getId()))
        .thenReturn(List.of(enrollment));
    when(studentCourseResultService.calculate(enrollment.getId()))
        .thenReturn(result(false, null, 5, 0));

    var results = promotionResultService.getPromotionResults(promotionId);

    assertEquals(1, results.size());
    var result = results.get(0);
    assertFalse(result.isComplete());
    assertFalse(result.isGraduate());
    assertEquals(0, result.getEarnedCredits());
    var year = result.getAcademicYears().get(0);
    assertFalse(year.isComplete());
    assertNull(year.getAverage());
  }

  @Test
  void earnedCreditsIncludeOnlyPassedCourses() {
    var student = student("STD-004");
    var passed = enrollment();
    var failed = enrollment();
    var failedYearId = UUID.randomUUID();
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of(student));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(student.getId()))
        .thenReturn(List.of(passed, failed));
    when(studentCourseResultService.calculate(passed.getId()))
        .thenReturn(result(true, new BigDecimal("15.00"), 3, 3));
    when(studentCourseResultService.calculate(failed.getId()))
        .thenReturn(result(true, new BigDecimal("9.00"), 4, 0, failedYearId));
    when(academicYearRepository.findById(failedYearId))
        .thenReturn(
            Optional.of(AcademicYearEntity.builder().id(failedYearId).label("2023-2024").build()));

    var results = promotionResultService.getPromotionResults(promotionId);

    var result = results.get(0);
    assertEquals(3, result.getEarnedCredits());
    assertEquals(7, result.getTotalCredits());
    assertEquals(2, result.getAcademicYears().size());
    assertFalse(result.isGraduate());
  }

  @Test
  void onlyStudentsFromRequestedPromotionAreReturned() {
    var studentA = student("STD-005");
    var studentB = student("STD-006");
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of(studentA, studentB));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(studentA.getId()))
        .thenReturn(List.of());
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(studentB.getId()))
        .thenReturn(List.of());

    var results = promotionResultService.getPromotionResults(promotionId);

    assertEquals(2, results.size());
    assertTrue(results.stream().anyMatch(r -> r.getStudentId().equals(studentA.getId())));
    assertTrue(results.stream().anyMatch(r -> r.getStudentId().equals(studentB.getId())));
    verify(userRepository).findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId);
  }

  @Test
  void getGraduatesReturnsOnlyGraduateStudents() {
    var studentA = student("STD-007");
    var studentB = student("STD-008");
    var enrollmentA = enrollment();
    var enrollmentB = enrollment();
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of(studentA, studentB));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(studentA.getId()))
        .thenReturn(List.of(enrollmentA));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(studentB.getId()))
        .thenReturn(List.of(enrollmentB));
    when(studentCourseResultService.calculate(enrollmentA.getId()))
        .thenReturn(result(true, new BigDecimal("14.00"), 5, 5));
    when(studentCourseResultService.calculate(enrollmentB.getId()))
        .thenReturn(result(true, new BigDecimal("9.00"), 5, 0));

    var graduates = promotionResultService.getGraduates(promotionId);

    assertEquals(1, graduates.size());
    assertEquals(studentA.getId(), graduates.get(0).getStudentId());
    assertEquals("STD-007", graduates.get(0).getStd());
    assertEquals("jane@hei.school", graduates.get(0).getEmail());
    assertEquals(5, graduates.get(0).getEarnedCredits());
  }

  private UserEntity student(String std) {
    return UserEntity.builder()
        .id(UUID.randomUUID())
        .firstName("Jane")
        .lastName("Doe")
        .email("jane@hei.school")
        .passwordHash("hash")
        .role(UserRoleEntity.STUDENT)
        .status(UserStatusEntity.ACTIVE)
        .std(std)
        .build();
  }

  private StudentCourseEnrollmentEntity enrollment() {
    return StudentCourseEnrollmentEntity.builder().id(UUID.randomUUID()).build();
  }

  private StudentCourseResult result(
      boolean complete, BigDecimal finalGrade, int credits, int earnedCredits) {
    return result(complete, finalGrade, credits, earnedCredits, yearId);
  }

  private StudentCourseResult result(
      boolean complete,
      BigDecimal finalGrade,
      int credits,
      int earnedCredits,
      UUID academicYearId) {
    return StudentCourseResult.builder()
        .enrollmentId(UUID.randomUUID())
        .courseId(UUID.randomUUID())
        .academicYearId(academicYearId)
        .complete(complete)
        .finalGrade(finalGrade)
        .credits(credits)
        .earnedCredits(earnedCredits)
        .build();
  }
}
