package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeRepository;
import com.example.demo.repository.StudentCourseEnrollmentRepository;
import com.example.demo.repository.model.AcademicYearEntity;
import com.example.demo.repository.model.CourseEntity;
import com.example.demo.repository.model.CourseOfferingEntity;
import com.example.demo.repository.model.ExamEntity;
import com.example.demo.repository.model.GradeEntity;
import com.example.demo.repository.model.StudentCourseEnrollmentEntity;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StudentCourseResultServiceTest {

  private StudentCourseEnrollmentRepository enrollmentRepository;
  private ExamRepository examRepository;
  private GradeRepository gradeRepository;
  private StudentCourseResultService service;

  @BeforeEach
  void setUp() {
    enrollmentRepository = mock(StudentCourseEnrollmentRepository.class);
    examRepository = mock(ExamRepository.class);
    gradeRepository = mock(GradeRepository.class);

    service =
        new StudentCourseResultService(
            enrollmentRepository, examRepository, gradeRepository, new CourseGradeCalculator());
  }

  @Test
  void shouldCalculateCompleteCourseResult() {
    UUID enrollmentId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    UUID academicYearId = UUID.randomUUID();
    UUID offeringId = UUID.randomUUID();
    UUID exam1Id = UUID.randomUUID();
    UUID exam2Id = UUID.randomUUID();

    StudentCourseEnrollmentEntity enrollment = mock(StudentCourseEnrollmentEntity.class);
    CourseEntity course = mock(CourseEntity.class);
    AcademicYearEntity academicYear = mock(AcademicYearEntity.class);
    CourseOfferingEntity offering = mock(CourseOfferingEntity.class);

    ExamEntity exam1 = ExamEntity.builder().id(exam1Id).coefficient(new BigDecimal("1")).build();

    ExamEntity exam2 = ExamEntity.builder().id(exam2Id).coefficient(new BigDecimal("2")).build();

    GradeEntity grade1 = GradeEntity.builder().exam(exam1).score(new BigDecimal("12")).build();

    GradeEntity grade2 = GradeEntity.builder().exam(exam2).score(new BigDecimal("16")).build();

    when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

    when(enrollment.getId()).thenReturn(enrollmentId);
    when(enrollment.getCourse()).thenReturn(course);
    when(enrollment.getAcademicYear()).thenReturn(academicYear);
    when(enrollment.getCourseOfferings()).thenReturn(Set.of(offering));

    when(course.getId()).thenReturn(courseId);
    when(course.getCredits()).thenReturn(5);
    when(academicYear.getId()).thenReturn(academicYearId);
    when(offering.getId()).thenReturn(offeringId);

    when(examRepository.findByCourseOffering_IdIn(java.util.List.of(offeringId)))
        .thenReturn(java.util.List.of(exam1, exam2));

    when(gradeRepository.findByStudentCourseEnrollment_Id(enrollmentId))
        .thenReturn(java.util.List.of(grade1, grade2));

    var result = service.calculate(enrollmentId);

    assertTrue(result.isComplete());
    assertEquals(new BigDecimal("14.67"), result.getFinalGrade());
    assertEquals(5, result.getCredits());
    assertEquals(5, result.getEarnedCredits());
    assertEquals(courseId, result.getCourseId());
    assertEquals(academicYearId, result.getAcademicYearId());
  }

  @Test
  void shouldBeIncompleteWhenOneExamGradeIsMissing() {
    UUID enrollmentId = UUID.randomUUID();
    UUID offeringId = UUID.randomUUID();
    UUID exam1Id = UUID.randomUUID();
    UUID exam2Id = UUID.randomUUID();

    StudentCourseEnrollmentEntity enrollment = mock(StudentCourseEnrollmentEntity.class);
    CourseEntity course = mock(CourseEntity.class);
    AcademicYearEntity academicYear = mock(AcademicYearEntity.class);
    CourseOfferingEntity offering = mock(CourseOfferingEntity.class);

    ExamEntity exam1 = ExamEntity.builder().id(exam1Id).coefficient(BigDecimal.ONE).build();

    ExamEntity exam2 = ExamEntity.builder().id(exam2Id).coefficient(BigDecimal.ONE).build();

    GradeEntity grade1 = GradeEntity.builder().exam(exam1).score(new BigDecimal("15")).build();

    when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

    when(enrollment.getCourse()).thenReturn(course);
    when(enrollment.getAcademicYear()).thenReturn(academicYear);
    when(enrollment.getCourseOfferings()).thenReturn(Set.of(offering));

    when(course.getCredits()).thenReturn(5);
    when(offering.getId()).thenReturn(offeringId);

    when(examRepository.findByCourseOffering_IdIn(java.util.List.of(offeringId)))
        .thenReturn(java.util.List.of(exam1, exam2));

    when(gradeRepository.findByStudentCourseEnrollment_Id(enrollmentId))
        .thenReturn(java.util.List.of(grade1));

    var result = service.calculate(enrollmentId);

    assertFalse(result.isComplete());
    assertNull(result.getFinalGrade());
    assertEquals(0, result.getEarnedCredits());
  }

  @Test
  void shouldNotEarnCreditsWhenFinalGradeIsBelowTen() {
    UUID enrollmentId = UUID.randomUUID();
    UUID offeringId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();

    StudentCourseEnrollmentEntity enrollment = mock(StudentCourseEnrollmentEntity.class);
    CourseEntity course = mock(CourseEntity.class);
    AcademicYearEntity academicYear = mock(AcademicYearEntity.class);
    CourseOfferingEntity offering = mock(CourseOfferingEntity.class);

    ExamEntity exam = ExamEntity.builder().id(examId).coefficient(BigDecimal.ONE).build();

    GradeEntity grade = GradeEntity.builder().exam(exam).score(new BigDecimal("9")).build();

    when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

    when(enrollment.getCourse()).thenReturn(course);
    when(enrollment.getAcademicYear()).thenReturn(academicYear);
    when(enrollment.getCourseOfferings()).thenReturn(Set.of(offering));

    when(course.getCredits()).thenReturn(6);
    when(offering.getId()).thenReturn(offeringId);

    when(examRepository.findByCourseOffering_IdIn(java.util.List.of(offeringId)))
        .thenReturn(java.util.List.of(exam));

    when(gradeRepository.findByStudentCourseEnrollment_Id(enrollmentId))
        .thenReturn(java.util.List.of(grade));

    var result = service.calculate(enrollmentId);

    assertTrue(result.isComplete());
    assertEquals(new BigDecimal("9.00"), result.getFinalGrade());
    assertEquals(0, result.getEarnedCredits());
  }
}
