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
import com.example.demo.model.Pathway;
import com.example.demo.model.StudentCourseResult;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.StudentCourseEnrollmentRepository;
import com.example.demo.repository.StudentGroupHistoryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.AcademicYearEntity;
import com.example.demo.repository.model.PathwayEntity;
import com.example.demo.repository.model.PromotionEntity;
import com.example.demo.repository.model.StudentCourseEnrollmentEntity;
import com.example.demo.repository.model.StudentGroupHistoryEntity;
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
  private StudentGroupHistoryRepository studentGroupHistoryRepository;
  private StudentCourseResultService studentCourseResultService;
  private PromotionResultService promotionResultService;

  private UUID promotionId;
  private UUID year1;
  private UUID year2;
  private UUID year3;

  @BeforeEach
  void setUp() {
    promotionRepository = mock(PromotionRepository.class);
    userRepository = mock(UserRepository.class);
    studentCourseEnrollmentRepository = mock(StudentCourseEnrollmentRepository.class);
    academicYearRepository = mock(AcademicYearRepository.class);
    studentGroupHistoryRepository = mock(StudentGroupHistoryRepository.class);
    studentCourseResultService = mock(StudentCourseResultService.class);

    promotionResultService =
        new PromotionResultService(
            promotionRepository,
            userRepository,
            studentCourseEnrollmentRepository,
            academicYearRepository,
            studentGroupHistoryRepository,
            studentCourseResultService);

    promotionId = UUID.randomUUID();
    year1 = UUID.randomUUID();
    year2 = UUID.randomUUID();
    year3 = UUID.randomUUID();
    when(promotionRepository.findById(promotionId))
        .thenReturn(Optional.of(PromotionEntity.builder().id(promotionId).name("Promo").build()));
    when(academicYearRepository.findById(year1))
        .thenReturn(Optional.of(AcademicYearEntity.builder().id(year1).label("2023-2024").build()));
    when(academicYearRepository.findById(year2))
        .thenReturn(Optional.of(AcademicYearEntity.builder().id(year2).label("2024-2025").build()));
    when(academicYearRepository.findById(year3))
        .thenReturn(Optional.of(AcademicYearEntity.builder().id(year3).label("2025-2026").build()));
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
  void resultsResponseContainsPromotionId() {
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of());

    var response = promotionResultService.getPromotionResults(promotionId);

    assertEquals(promotionId, response.getPromotionId());
    assertTrue(response.getStudents().isEmpty());
  }

  @Test
  void onlyOneAcademicYearIsNotCompleteNorGraduate() {
    var student = student("STD-001");
    var enrollment = enrollment();
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of(student));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(student.getId()))
        .thenReturn(List.of(enrollment));
    when(studentCourseResultService.calculate(enrollment.getId()))
        .thenReturn(result(true, new BigDecimal("12.00"), 5, 5, year1));

    var result = promotionResultService.getPromotionResults(promotionId).getStudents().get(0);

    assertFalse(result.isComplete());
    assertFalse(result.isGraduate());
  }

  @Test
  void onlyTwoAcademicYearsAreNotCompleteNorGraduate() {
    var student = student("STD-002");
    var enrollmentA = enrollment();
    var enrollmentB = enrollment();
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of(student));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(student.getId()))
        .thenReturn(List.of(enrollmentA, enrollmentB));
    when(studentCourseResultService.calculate(enrollmentA.getId()))
        .thenReturn(result(true, new BigDecimal("12.00"), 5, 5, year1));
    when(studentCourseResultService.calculate(enrollmentB.getId()))
        .thenReturn(result(true, new BigDecimal("13.00"), 5, 5, year2));

    var result = promotionResultService.getPromotionResults(promotionId).getStudents().get(0);

    assertFalse(result.isComplete());
    assertFalse(result.isGraduate());
  }

  @Test
  void threeAcademicYearsAllPassingMakesGraduate() {
    var student = student("STD-003");
    var enrollmentA = enrollment();
    var enrollmentB = enrollment();
    var enrollmentC = enrollment();
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of(student));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(student.getId()))
        .thenReturn(List.of(enrollmentA, enrollmentB, enrollmentC));
    when(studentCourseResultService.calculate(enrollmentA.getId()))
        .thenReturn(result(true, new BigDecimal("12.00"), 5, 5, year1));
    when(studentCourseResultService.calculate(enrollmentB.getId()))
        .thenReturn(result(true, new BigDecimal("13.00"), 5, 5, year2));
    when(studentCourseResultService.calculate(enrollmentC.getId()))
        .thenReturn(result(true, new BigDecimal("14.00"), 5, 5, year3));

    var result = promotionResultService.getPromotionResults(promotionId).getStudents().get(0);

    assertTrue(result.isComplete());
    assertTrue(result.isGraduate());
    assertEquals(3, result.getAcademicYears().size());
    var firstYear = result.getAcademicYears().get(0);
    assertTrue(firstYear.isComplete());
    assertEquals(new BigDecimal("12.00"), firstYear.getAverage());
    assertEquals(5, firstYear.getEarnedCredits());
    assertEquals("2023-2024", firstYear.getAcademicYearLabel());
  }

  @Test
  void threeAcademicYearsWithOneFailedCourseIsCompleteButNotGraduate() {
    var student = student("STD-004");
    var enrollmentA = enrollment();
    var enrollmentB = enrollment();
    var enrollmentC = enrollment();
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of(student));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(student.getId()))
        .thenReturn(List.of(enrollmentA, enrollmentB, enrollmentC));
    when(studentCourseResultService.calculate(enrollmentA.getId()))
        .thenReturn(result(true, new BigDecimal("12.00"), 5, 5, year1));
    when(studentCourseResultService.calculate(enrollmentB.getId()))
        .thenReturn(result(true, new BigDecimal("13.00"), 5, 5, year2));
    when(studentCourseResultService.calculate(enrollmentC.getId()))
        .thenReturn(result(true, new BigDecimal("8.00"), 5, 0, year3));

    var result = promotionResultService.getPromotionResults(promotionId).getStudents().get(0);

    assertTrue(result.isComplete());
    assertFalse(result.isGraduate());
    assertEquals(0, result.getAcademicYears().get(2).getEarnedCredits());
    assertEquals(new BigDecimal("8.00"), result.getAcademicYears().get(2).getAverage());
  }

  @Test
  void threeAcademicYearsWithOneIncompleteCourseIsNotCompleteNorGraduate() {
    var student = student("STD-005");
    var enrollmentA = enrollment();
    var enrollmentB = enrollment();
    var enrollmentC = enrollment();
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of(student));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(student.getId()))
        .thenReturn(List.of(enrollmentA, enrollmentB, enrollmentC));
    when(studentCourseResultService.calculate(enrollmentA.getId()))
        .thenReturn(result(true, new BigDecimal("12.00"), 5, 5, year1));
    when(studentCourseResultService.calculate(enrollmentB.getId()))
        .thenReturn(result(true, new BigDecimal("13.00"), 5, 5, year2));
    when(studentCourseResultService.calculate(enrollmentC.getId()))
        .thenReturn(result(false, null, 5, 0, year3));

    var result = promotionResultService.getPromotionResults(promotionId).getStudents().get(0);

    assertFalse(result.isComplete());
    assertFalse(result.isGraduate());
    var incompleteYear = result.getAcademicYears().get(2);
    assertFalse(incompleteYear.isComplete());
    assertNull(incompleteYear.getAverage());
  }

  @Test
  void yearEarnedCreditsIncludeOnlyPassedCourses() {
    var student = student("STD-006");
    var passed = enrollment();
    var failed = enrollment();
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of(student));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(student.getId()))
        .thenReturn(List.of(passed, failed));
    when(studentCourseResultService.calculate(passed.getId()))
        .thenReturn(result(true, new BigDecimal("15.00"), 3, 3, year1));
    when(studentCourseResultService.calculate(failed.getId()))
        .thenReturn(result(true, new BigDecimal("9.00"), 4, 0, year1));

    var result = promotionResultService.getPromotionResults(promotionId).getStudents().get(0);

    assertEquals(3, result.getAcademicYears().get(0).getEarnedCredits());
    assertFalse(result.isGraduate());
  }

  @Test
  void onlyStudentsFromRequestedPromotionAreReturned() {
    var studentA = student("STD-007");
    var studentB = student("STD-008");
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of(studentA, studentB));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(studentA.getId()))
        .thenReturn(List.of());
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(studentB.getId()))
        .thenReturn(List.of());

    var results = promotionResultService.getPromotionResults(promotionId).getStudents();

    assertEquals(2, results.size());
    assertTrue(results.stream().anyMatch(r -> r.getStudentId().equals(studentA.getId())));
    assertTrue(results.stream().anyMatch(r -> r.getStudentId().equals(studentB.getId())));
    verify(userRepository).findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId);
  }

  @Test
  void getGraduatesReturnsOnlyGraduateStudents() {
    var graduate = student("STD-009");
    var nonGraduate = student("STD-010");
    var graduateEnrollments = List.of(enrollment(), enrollment(), enrollment());
    var nonGraduateEnrollments = List.of(enrollment(), enrollment(), enrollment());
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of(graduate, nonGraduate));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(graduate.getId()))
        .thenReturn(graduateEnrollments);
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(nonGraduate.getId()))
        .thenReturn(nonGraduateEnrollments);
    when(studentCourseResultService.calculate(graduateEnrollments.get(0).getId()))
        .thenReturn(result(true, new BigDecimal("12.00"), 5, 5, year1));
    when(studentCourseResultService.calculate(graduateEnrollments.get(1).getId()))
        .thenReturn(result(true, new BigDecimal("13.00"), 5, 5, year2));
    when(studentCourseResultService.calculate(graduateEnrollments.get(2).getId()))
        .thenReturn(result(true, new BigDecimal("14.00"), 5, 5, year3));
    when(studentCourseResultService.calculate(nonGraduateEnrollments.get(0).getId()))
        .thenReturn(result(true, new BigDecimal("12.00"), 5, 5, year1));
    when(studentCourseResultService.calculate(nonGraduateEnrollments.get(1).getId()))
        .thenReturn(result(true, new BigDecimal("13.00"), 5, 5, year2));
    when(studentCourseResultService.calculate(nonGraduateEnrollments.get(2).getId()))
        .thenReturn(result(true, new BigDecimal("9.00"), 5, 0, year3));

    when(studentGroupHistoryRepository.findByStudent_IdAndEndedAtIsNull(graduate.getId()))
        .thenReturn(
            Optional.of(
                StudentGroupHistoryEntity.builder()
                    .id(UUID.randomUUID())
                    .pathway(PathwayEntity.EL)
                    .build()));

    var graduates = promotionResultService.getGraduates(promotionId);

    assertEquals(1, graduates.size());
    var response = graduates.get(0);
    assertEquals(graduate.getId(), response.getStudentId());
    assertEquals("STD-009", response.getStd());
    assertEquals(Pathway.EL, response.getPathway());
    assertEquals(new BigDecimal("13.00"), response.getOverallAverage());
  }

  @Test
  void graduateWithoutGroupHistoryHasNullPathway() {
    var graduate = student("STD-011");
    var enrollments = List.of(enrollment(), enrollment(), enrollment());
    when(userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId))
        .thenReturn(List.of(graduate));
    when(studentCourseEnrollmentRepository.findAllByStudent_Id(graduate.getId()))
        .thenReturn(enrollments);
    when(studentCourseResultService.calculate(enrollments.get(0).getId()))
        .thenReturn(result(true, new BigDecimal("12.00"), 5, 5, year1));
    when(studentCourseResultService.calculate(enrollments.get(1).getId()))
        .thenReturn(result(true, new BigDecimal("13.00"), 5, 5, year2));
    when(studentCourseResultService.calculate(enrollments.get(2).getId()))
        .thenReturn(result(true, new BigDecimal("14.00"), 5, 5, year3));
    when(studentGroupHistoryRepository.findByStudent_IdAndEndedAtIsNull(graduate.getId()))
        .thenReturn(Optional.empty());
    when(studentGroupHistoryRepository.findByStudent_IdOrderByStartedAtAsc(graduate.getId()))
        .thenReturn(List.of());

    var graduates = promotionResultService.getGraduates(promotionId);

    assertEquals(1, graduates.size());
    assertNull(graduates.get(0).getPathway());
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
