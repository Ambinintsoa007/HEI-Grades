package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.GradeHistoryResponse;
import com.example.demo.endpoint.rest.dto.GradeResponse;
import com.example.demo.endpoint.rest.dto.SaveGradeRequest;
import com.example.demo.endpoint.rest.exception.BusinessException;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.mapper.GradeHistoryMapper;
import com.example.demo.mapper.GradeMapper;
import com.example.demo.model.Grade;
import com.example.demo.model.GradeHistory;
import com.example.demo.model.SaveGradeResult;
import com.example.demo.model.UserRole;
import com.example.demo.repository.CourseOfferingTeacherRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeHistoryRepository;
import com.example.demo.repository.GradeRepository;
import com.example.demo.repository.StudentCourseEnrollmentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final GradeHistoryRepository gradeHistoryRepository;
  private final ExamRepository examRepository;
  private final StudentCourseEnrollmentRepository studentCourseEnrollmentRepository;
  private final CourseOfferingTeacherRepository courseOfferingTeacherRepository;
  private final GradeMapper gradeMapper;
  private final GradeHistoryMapper gradeHistoryMapper;

  @Transactional
  public SaveGradeResult save(UUID authenticatedUserId, UserRole role, SaveGradeRequest request) {

    if (role != UserRole.ADMIN && role != UserRole.TEACHER) {
      throw new AccessDeniedException("Only admins and teachers can manage grades");
    }

    var exam =
        examRepository
            .findById(request.getExamId())
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

    var offering = exam.getCourseOffering();

    if (role == UserRole.TEACHER
        && !courseOfferingTeacherRepository.existsByCourseOffering_IdAndTeacher_Id(
            offering.getId(), authenticatedUserId)) {
      throw new AccessDeniedException("Teacher is not assigned to this course offering");
    }

    var enrollment =
        studentCourseEnrollmentRepository
            .findById(request.getStudentCourseEnrollmentId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Student course enrollment not found"));

    boolean followsOffering =
        enrollment.getCourseOfferings().stream()
            .anyMatch(current -> Objects.equals(current.getId(), offering.getId()));

    if (!followsOffering) {
      throw new BusinessException(
          "Student course enrollment does not belong to this course offering");
    }

    var existingGrade =
        gradeRepository.findByExam_IdAndStudentCourseEnrollment_Id(
            exam.getId(), enrollment.getId());

    if (existingGrade.isEmpty()) {
      Instant now = Instant.now();

      Grade grade =
          Grade.builder()
              .id(UUID.randomUUID())
              .examId(exam.getId())
              .studentCourseEnrollmentId(enrollment.getId())
              .score(request.getScore())
              .updatedAt(now)
              .build();

      Grade saved = gradeMapper.toDomain(gradeRepository.save(gradeMapper.toEntity(grade)));

      return new SaveGradeResult(toResponse(saved), true);
    }

    var gradeEntity = existingGrade.get();

    if (gradeEntity.getScore().compareTo(request.getScore()) == 0) {
      throw new BusinessException("New score must be different from current score");
    }

    if (request.getCorrectionReason() == null || request.getCorrectionReason().isBlank()) {
      throw new BusinessException("Correction reason is required when correcting a grade");
    }

    var oldScore = gradeEntity.getScore();
    Instant now = Instant.now();

    gradeEntity.setScore(request.getScore());
    gradeEntity.setUpdatedAt(now);

    var savedGrade = gradeRepository.save(gradeEntity);

    GradeHistory history =
        GradeHistory.builder()
            .id(UUID.randomUUID())
            .gradeId(savedGrade.getId())
            .oldScore(oldScore)
            .newScore(request.getScore())
            .reason(request.getCorrectionReason().trim())
            .changedBy(authenticatedUserId)
            .changedAt(now)
            .build();

    gradeHistoryRepository.save(gradeHistoryMapper.toEntity(history));

    return new SaveGradeResult(toResponse(gradeMapper.toDomain(savedGrade)), false);
  }

  @Transactional(readOnly = true)
  public List<GradeHistoryResponse> getHistory(
      UUID authenticatedUserId, UserRole role, UUID gradeId) {

    var grade =
        gradeRepository
            .findById(gradeId)
            .orElseThrow(() -> new ResourceNotFoundException("Grade not found"));

    UUID offeringId = grade.getExam().getCourseOffering().getId();

    if (role != UserRole.ADMIN
        && !(role == UserRole.TEACHER
            && courseOfferingTeacherRepository.existsByCourseOffering_IdAndTeacher_Id(
                offeringId, authenticatedUserId))) {
      throw new AccessDeniedException("Access denied");
    }

    return gradeHistoryRepository.findByGrade_IdOrderByChangedAtAsc(gradeId).stream()
        .map(gradeHistoryMapper::toDomain)
        .map(
            history ->
                GradeHistoryResponse.builder()
                    .id(history.getId())
                    .gradeId(history.getGradeId())
                    .oldScore(history.getOldScore())
                    .newScore(history.getNewScore())
                    .reason(history.getReason())
                    .changedBy(history.getChangedBy())
                    .changedAt(history.getChangedAt())
                    .build())
        .toList();
  }

  @Transactional(readOnly = true)
  public List<GradeResponse> getExamGrades(UUID authenticatedUserId, UserRole role, UUID examId) {

    var exam =
        examRepository
            .findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

    UUID offeringId = exam.getCourseOffering().getId();

    if (role != UserRole.ADMIN
        && !(role == UserRole.TEACHER
            && courseOfferingTeacherRepository.existsByCourseOffering_IdAndTeacher_Id(
                offeringId, authenticatedUserId))) {
      throw new AccessDeniedException("Access denied");
    }

    return gradeRepository.findByExam_Id(examId).stream()
        .map(gradeMapper::toDomain)
        .map(this::toResponse)
        .toList();
  }

  private GradeResponse toResponse(Grade grade) {
    return GradeResponse.builder()
        .id(grade.getId())
        .examId(grade.getExamId())
        .studentCourseEnrollmentId(grade.getStudentCourseEnrollmentId())
        .score(grade.getScore())
        .updatedAt(grade.getUpdatedAt())
        .build();
  }
}
