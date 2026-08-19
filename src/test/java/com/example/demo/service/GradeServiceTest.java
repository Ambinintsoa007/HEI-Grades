package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.endpoint.rest.dto.SaveGradeRequest;
import com.example.demo.endpoint.rest.exception.BusinessException;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.mapper.GradeHistoryMapper;
import com.example.demo.mapper.GradeMapper;
import com.example.demo.model.Grade;
import com.example.demo.model.GradeHistory;
import com.example.demo.model.UserRole;
import com.example.demo.repository.CourseOfferingTeacherRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeHistoryRepository;
import com.example.demo.repository.GradeRepository;
import com.example.demo.repository.StudentCourseEnrollmentRepository;
import com.example.demo.repository.model.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class GradeServiceTest {

  private GradeRepository gradeRepository;
  private GradeHistoryRepository gradeHistoryRepository;
  private ExamRepository examRepository;
  private StudentCourseEnrollmentRepository enrollmentRepository;
  private CourseOfferingTeacherRepository courseOfferingTeacherRepository;
  private GradeMapper gradeMapper;
  private GradeHistoryMapper gradeHistoryMapper;

  private GradeService gradeService;

  @BeforeEach
  void setUp() {
    gradeRepository = mock(GradeRepository.class);
    gradeHistoryRepository = mock(GradeHistoryRepository.class);
    examRepository = mock(ExamRepository.class);
    enrollmentRepository = mock(StudentCourseEnrollmentRepository.class);
    courseOfferingTeacherRepository = mock(CourseOfferingTeacherRepository.class);
    gradeMapper = mock(GradeMapper.class);
    gradeHistoryMapper = mock(GradeHistoryMapper.class);

    gradeService =
        new GradeService(
            gradeRepository,
            gradeHistoryRepository,
            examRepository,
            enrollmentRepository,
            courseOfferingTeacherRepository,
            gradeMapper,
            gradeHistoryMapper);
  }

  @Test
  void adminShouldCreateFirstGrade() {
    UUID adminId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID enrollmentId = UUID.randomUUID();

    var exam = mock(ExamEntity.class);
    var offering = mock(CourseOfferingEntity.class);
    var enrollment = mock(StudentCourseEnrollmentEntity.class);

    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(exam.getCourseOffering()).thenReturn(offering);
    when(offering.getId()).thenReturn(UUID.randomUUID());

    when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
    when(enrollment.getId()).thenReturn(enrollmentId);
    when(enrollment.getCourseOfferings()).thenReturn(Set.of(offering));

    when(gradeRepository.findByExam_IdAndStudentCourseEnrollment_Id(examId, enrollmentId))
        .thenReturn(Optional.empty());

    GradeEntity savedEntity = mock(GradeEntity.class);

    Grade savedGrade =
        Grade.builder()
            .id(UUID.randomUUID())
            .examId(examId)
            .studentCourseEnrollmentId(enrollmentId)
            .score(new BigDecimal("15"))
            .updatedAt(Instant.now())
            .build();

    when(gradeMapper.toEntity(any(Grade.class))).thenReturn(mock(GradeEntity.class));
    when(gradeRepository.save(any(GradeEntity.class))).thenReturn(savedEntity);
    when(gradeMapper.toDomain(savedEntity)).thenReturn(savedGrade);

    var result =
        gradeService.save(adminId, UserRole.ADMIN, request(examId, enrollmentId, "15", null));

    assertTrue(result.created());
    assertEquals(new BigDecimal("15"), result.grade().getScore());

    verify(gradeHistoryRepository, never()).save(any());
  }

  @Test
  void assignedTeacherShouldCreateGrade() {
    UUID teacherId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID enrollmentId = UUID.randomUUID();
    UUID offeringId = UUID.randomUUID();

    var exam = mock(ExamEntity.class);
    var offering = mock(CourseOfferingEntity.class);
    var enrollment = mock(StudentCourseEnrollmentEntity.class);

    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(exam.getCourseOffering()).thenReturn(offering);
    when(offering.getId()).thenReturn(offeringId);

    when(courseOfferingTeacherRepository.existsByCourseOffering_IdAndTeacher_Id(
            offeringId, teacherId))
        .thenReturn(true);

    when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
    when(enrollment.getId()).thenReturn(enrollmentId);
    when(enrollment.getCourseOfferings()).thenReturn(Set.of(offering));

    when(gradeRepository.findByExam_IdAndStudentCourseEnrollment_Id(examId, enrollmentId))
        .thenReturn(Optional.empty());

    GradeEntity savedEntity = mock(GradeEntity.class);

    Grade savedGrade =
        Grade.builder()
            .id(UUID.randomUUID())
            .examId(examId)
            .studentCourseEnrollmentId(enrollmentId)
            .score(new BigDecimal("14"))
            .updatedAt(Instant.now())
            .build();

    when(gradeMapper.toEntity(any(Grade.class))).thenReturn(mock(GradeEntity.class));
    when(gradeRepository.save(any(GradeEntity.class))).thenReturn(savedEntity);
    when(gradeMapper.toDomain(savedEntity)).thenReturn(savedGrade);

    var result =
        gradeService.save(teacherId, UserRole.TEACHER, request(examId, enrollmentId, "14", null));

    assertTrue(result.created());
  }

  @Test
  void unassignedTeacherShouldBeRejected() {
    UUID teacherId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();

    var exam = mock(ExamEntity.class);
    var offering = mock(CourseOfferingEntity.class);

    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(exam.getCourseOffering()).thenReturn(offering);
    when(offering.getId()).thenReturn(UUID.randomUUID());

    assertThrows(
        AccessDeniedException.class,
        () ->
            gradeService.save(
                teacherId, UserRole.TEACHER, request(examId, UUID.randomUUID(), "12", null)));
  }

  @Test
  void studentShouldNotManageGrades() {
    assertThrows(
        AccessDeniedException.class,
        () ->
            gradeService.save(
                UUID.randomUUID(),
                UserRole.STUDENT,
                request(UUID.randomUUID(), UUID.randomUUID(), "12", null)));
  }

  @Test
  void shouldRejectEnrollmentNotLinkedToOffering() {
    UUID examId = UUID.randomUUID();
    UUID enrollmentId = UUID.randomUUID();

    CourseOfferingEntity offering = CourseOfferingEntity.builder().id(UUID.randomUUID()).build();

    CourseOfferingEntity anotherOffering =
        CourseOfferingEntity.builder().id(UUID.randomUUID()).build();

    ExamEntity exam =
        ExamEntity.builder()
            .id(examId)
            .courseOffering(offering)
            .coefficient(BigDecimal.ONE)
            .build();

    StudentCourseEnrollmentEntity enrollment =
        StudentCourseEnrollmentEntity.builder()
            .id(enrollmentId)
            .courseOfferings(new java.util.HashSet<>(Set.of(anotherOffering)))
            .build();

    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

    when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

    assertThrows(
        BusinessException.class,
        () ->
            gradeService.save(
                UUID.randomUUID(), UserRole.ADMIN, request(examId, enrollmentId, "15", null)));

    verify(gradeRepository, never()).save(any());
  }

  @Test
  void shouldRequireCorrectionReasonWhenChangingGrade() {
    UUID examId = UUID.randomUUID();
    UUID enrollmentId = UUID.randomUUID();

    CourseOfferingEntity offering = CourseOfferingEntity.builder().id(UUID.randomUUID()).build();

    ExamEntity exam =
        ExamEntity.builder()
            .id(examId)
            .courseOffering(offering)
            .coefficient(BigDecimal.ONE)
            .build();

    StudentCourseEnrollmentEntity enrollment =
        StudentCourseEnrollmentEntity.builder()
            .id(enrollmentId)
            .courseOfferings(new java.util.HashSet<>(Set.of(offering)))
            .build();

    GradeEntity existingGrade =
        GradeEntity.builder()
            .id(UUID.randomUUID())
            .exam(exam)
            .studentCourseEnrollment(enrollment)
            .score(new BigDecimal("10"))
            .updatedAt(Instant.now())
            .build();

    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

    when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

    when(gradeRepository.findByExam_IdAndStudentCourseEnrollment_Id(examId, enrollmentId))
        .thenReturn(Optional.of(existingGrade));

    assertThrows(
        BusinessException.class,
        () ->
            gradeService.save(
                UUID.randomUUID(), UserRole.ADMIN, request(examId, enrollmentId, "15", null)));

    verify(gradeRepository, never()).save(any());
    verify(gradeHistoryRepository, never()).save(any());
  }

  @Test
  void shouldCorrectGradeAndAppendHistory() {
    UUID adminId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID enrollmentId = UUID.randomUUID();
    UUID gradeId = UUID.randomUUID();

    CourseOfferingEntity offering = CourseOfferingEntity.builder().id(UUID.randomUUID()).build();

    ExamEntity exam =
        ExamEntity.builder()
            .id(examId)
            .courseOffering(offering)
            .coefficient(BigDecimal.ONE)
            .build();

    StudentCourseEnrollmentEntity enrollment =
        StudentCourseEnrollmentEntity.builder()
            .id(enrollmentId)
            .courseOfferings(new java.util.HashSet<>(Set.of(offering)))
            .build();

    GradeEntity existingGrade =
        GradeEntity.builder()
            .id(gradeId)
            .exam(exam)
            .studentCourseEnrollment(enrollment)
            .score(new BigDecimal("10"))
            .updatedAt(Instant.now())
            .build();

    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

    when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

    when(gradeRepository.findByExam_IdAndStudentCourseEnrollment_Id(examId, enrollmentId))
        .thenReturn(Optional.of(existingGrade));

    when(gradeRepository.save(existingGrade)).thenReturn(existingGrade);

    when(gradeMapper.toDomain(existingGrade))
        .thenAnswer(
            invocation ->
                Grade.builder()
                    .id(existingGrade.getId())
                    .examId(examId)
                    .studentCourseEnrollmentId(enrollmentId)
                    .score(existingGrade.getScore())
                    .updatedAt(existingGrade.getUpdatedAt())
                    .build());

    when(gradeHistoryMapper.toEntity(any(GradeHistory.class)))
        .thenReturn(mock(GradeHistoryEntity.class));

    var result =
        gradeService.save(
            adminId,
            UserRole.ADMIN,
            request(examId, enrollmentId, "15", "Correction after review"));

    assertFalse(result.created());
    assertEquals(new BigDecimal("15"), result.grade().getScore());
    assertEquals(new BigDecimal("15"), existingGrade.getScore());

    verify(gradeRepository).save(existingGrade);
    verify(gradeHistoryRepository).save(any(GradeHistoryEntity.class));
  }

  @Test
  void adminShouldReadHistoryInAscendingOrder() {
    UUID gradeId = UUID.randomUUID();

    var gradeEntity = mock(GradeEntity.class);
    var exam = mock(ExamEntity.class);
    var offering = mock(CourseOfferingEntity.class);

    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(gradeEntity));

    when(gradeEntity.getExam()).thenReturn(exam);
    when(exam.getCourseOffering()).thenReturn(offering);
    when(offering.getId()).thenReturn(UUID.randomUUID());

    GradeHistoryEntity historyEntity = mock(GradeHistoryEntity.class);

    GradeHistory history =
        GradeHistory.builder()
            .id(UUID.randomUUID())
            .gradeId(gradeId)
            .oldScore(new BigDecimal("10"))
            .newScore(new BigDecimal("15"))
            .reason("Correction")
            .changedBy(UUID.randomUUID())
            .changedAt(Instant.now())
            .build();

    when(gradeHistoryRepository.findByGrade_IdOrderByChangedAtAsc(gradeId))
        .thenReturn(List.of(historyEntity));

    when(gradeHistoryMapper.toDomain(historyEntity)).thenReturn(history);

    var result = gradeService.getHistory(UUID.randomUUID(), UserRole.ADMIN, gradeId);

    assertEquals(1, result.size());
    assertEquals(new BigDecimal("10"), result.getFirst().getOldScore());
    assertEquals(new BigDecimal("15"), result.getFirst().getNewScore());

    verify(gradeHistoryRepository).findByGrade_IdOrderByChangedAtAsc(gradeId);
  }

  @Test
  void assignedTeacherShouldGetExamGrades() {
    UUID teacherId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID offeringId = UUID.randomUUID();

    var offering = CourseOfferingEntity.builder().id(offeringId).build();

    var exam =
        ExamEntity.builder()
            .id(examId)
            .courseOffering(offering)
            .coefficient(BigDecimal.ONE)
            .build();

    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

    when(courseOfferingTeacherRepository.existsByCourseOffering_IdAndTeacher_Id(
            offeringId, teacherId))
        .thenReturn(true);

    when(enrollmentRepository.findByCourseOfferings_Id(offeringId)).thenReturn(List.of());

    var result = gradeService.getExamGrades(teacherId, UserRole.TEACHER, examId);

    assertTrue(result.isEmpty());
  }

  @Test
  void unassignedTeacherShouldNotGetExamGrades() {
    UUID teacherId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID offeringId = UUID.randomUUID();

    var offering = CourseOfferingEntity.builder().id(offeringId).build();

    var exam =
        ExamEntity.builder()
            .id(examId)
            .courseOffering(offering)
            .coefficient(BigDecimal.ONE)
            .build();

    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

    when(courseOfferingTeacherRepository.existsByCourseOffering_IdAndTeacher_Id(
            offeringId, teacherId))
        .thenReturn(false);

    assertThrows(
        AccessDeniedException.class,
        () -> gradeService.getExamGrades(teacherId, UserRole.TEACHER, examId));

    verify(enrollmentRepository, never()).findByCourseOfferings_Id(any());
  }

  @Test
  void shouldRejectUnknownExamWhenGettingGrades() {
    UUID examId = UUID.randomUUID();

    when(examRepository.findById(examId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> gradeService.getExamGrades(UUID.randomUUID(), UserRole.ADMIN, examId));
  }

  @Test
  void studentShouldGetOnlyOwnGrades() {
    UUID studentId = UUID.randomUUID();
    UUID gradeId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();

    var course = CourseEntity.builder().id(courseId).ref("PROG4").build();

    var enrollment =
        StudentCourseEnrollmentEntity.builder().id(UUID.randomUUID()).course(course).build();

    var offering = CourseOfferingEntity.builder().id(UUID.randomUUID()).course(course).build();

    var exam =
        ExamEntity.builder()
            .id(examId)
            .ref("EXAM-1")
            .courseOffering(offering)
            .coefficient(new BigDecimal("2"))
            .build();

    var grade =
        GradeEntity.builder()
            .id(gradeId)
            .exam(exam)
            .studentCourseEnrollment(enrollment)
            .score(new BigDecimal("16"))
            .updatedAt(Instant.now())
            .build();

    when(gradeRepository.findByStudentCourseEnrollment_Student_Id(studentId))
        .thenReturn(List.of(grade));

    var result = gradeService.getCurrentStudentGrades(studentId, UserRole.STUDENT);

    assertEquals(1, result.size());

    var response = result.getFirst();

    assertEquals(gradeId, response.getGradeId());
    assertEquals(examId, response.getExamId());
    assertEquals("EXAM-1", response.getExamRef());
    assertEquals(courseId, response.getCourseId());
    assertEquals("PROG4", response.getCourseRef());
    assertEquals(new BigDecimal("16"), response.getScore());
    assertEquals(new BigDecimal("2"), response.getCoefficient());

    verify(gradeRepository).findByStudentCourseEnrollment_Student_Id(studentId);
  }

  @Test
  void studentWithoutGradesShouldGetEmptyList() {
    UUID studentId = UUID.randomUUID();

    when(gradeRepository.findByStudentCourseEnrollment_Student_Id(studentId)).thenReturn(List.of());

    var result = gradeService.getCurrentStudentGrades(studentId, UserRole.STUDENT);

    assertTrue(result.isEmpty());
  }

  @Test
  void nonStudentShouldNotGetCurrentStudentGrades() {
    UUID userId = UUID.randomUUID();

    assertThrows(
        AccessDeniedException.class,
        () -> gradeService.getCurrentStudentGrades(userId, UserRole.ADMIN));

    assertThrows(
        AccessDeniedException.class,
        () -> gradeService.getCurrentStudentGrades(userId, UserRole.TEACHER));

    verify(gradeRepository, never()).findByStudentCourseEnrollment_Student_Id(any());
  }

  @Test
  void adminShouldGetExamGradesIncludingMissingGrades() {
    UUID adminId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID offeringId = UUID.randomUUID();

    var offering = CourseOfferingEntity.builder().id(offeringId).build();

    var exam =
        ExamEntity.builder()
            .id(examId)
            .ref("EXAM-1")
            .courseOffering(offering)
            .coefficient(BigDecimal.ONE)
            .build();

    UUID student1Id = UUID.randomUUID();
    UUID student2Id = UUID.randomUUID();

    var student1 =
        UserEntity.builder()
            .id(student1Id)
            .std("STD001")
            .firstName("Alice")
            .lastName("Doe")
            .build();

    var student2 =
        UserEntity.builder()
            .id(student2Id)
            .std("STD002")
            .firstName("Bob")
            .lastName("Smith")
            .build();

    UUID enrollment1Id = UUID.randomUUID();
    UUID enrollment2Id = UUID.randomUUID();

    var enrollment1 =
        StudentCourseEnrollmentEntity.builder().id(enrollment1Id).student(student1).build();

    var enrollment2 =
        StudentCourseEnrollmentEntity.builder().id(enrollment2Id).student(student2).build();

    UUID gradeId = UUID.randomUUID();

    var grade =
        GradeEntity.builder()
            .id(gradeId)
            .exam(exam)
            .studentCourseEnrollment(enrollment1)
            .score(new BigDecimal("15"))
            .updatedAt(Instant.now())
            .build();

    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

    when(enrollmentRepository.findByCourseOfferings_Id(offeringId))
        .thenReturn(List.of(enrollment1, enrollment2));

    when(gradeRepository.findByExam_IdAndStudentCourseEnrollment_Id(examId, enrollment1Id))
        .thenReturn(Optional.of(grade));

    when(gradeRepository.findByExam_IdAndStudentCourseEnrollment_Id(examId, enrollment2Id))
        .thenReturn(Optional.empty());

    var result = gradeService.getExamGrades(adminId, UserRole.ADMIN, examId);

    assertEquals(2, result.size());

    var first = result.get(0);
    assertEquals(gradeId, first.getGradeId());
    assertEquals(student1Id, first.getStudentId());
    assertEquals("STD001", first.getStd());
    assertEquals(new BigDecimal("15"), first.getScore());

    var second = result.get(1);
    assertNull(second.getGradeId());
    assertEquals(student2Id, second.getStudentId());
    assertEquals("STD002", second.getStd());
    assertNull(second.getScore());
  }

  private SaveGradeRequest request(
      UUID examId, UUID enrollmentId, String score, String correctionReason) {

    SaveGradeRequest request = new SaveGradeRequest();
    request.setExamId(examId);
    request.setStudentCourseEnrollmentId(enrollmentId);
    request.setScore(new BigDecimal(score));
    request.setCorrectionReason(correctionReason);
    return request;
  }
}
