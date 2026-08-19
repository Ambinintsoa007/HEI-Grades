package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.TranscriptCourseResponse;
import com.example.demo.endpoint.rest.dto.TranscriptResponse;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.model.UserRole;
import com.example.demo.repository.StudentCourseEnrollmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.UserRoleEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TranscriptService {

  private static final BigDecimal PASSING_GRADE = new BigDecimal("10");

  private final UserRepository userRepository;
  private final StudentCourseEnrollmentRepository studentCourseEnrollmentRepository;
  private final StudentCourseResultService studentCourseResultService;

  @Transactional(readOnly = true)
  public TranscriptResponse getTranscript(UUID authenticatedUserId, UserRole role, UUID studentId) {

    var student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

    if (student.getRole() != UserRoleEntity.STUDENT) {
      throw new ResourceNotFoundException("Student not found");
    }

    if (role == UserRole.STUDENT && !authenticatedUserId.equals(studentId)) {
      throw new AccessDeniedException("Access denied");
    }

    if (role != UserRole.STUDENT && role != UserRole.ADMIN) {
      throw new AccessDeniedException("Access denied");
    }

    var enrollments = studentCourseEnrollmentRepository.findAllByStudent_Id(studentId);

    var courses = new ArrayList<TranscriptCourseResponse>();

    boolean complete = true;
    int earnedCredits = 0;

    BigDecimal weightedTotal = BigDecimal.ZERO;
    int totalCredits = 0;

    for (var enrollment : enrollments) {

      var result = studentCourseResultService.calculate(enrollment.getId());

      var course = enrollment.getCourse();

      Boolean validated = null;

      if (result.isComplete()) {
        validated = result.getFinalGrade().compareTo(PASSING_GRADE) >= 0;

        weightedTotal =
            weightedTotal.add(
                result.getFinalGrade().multiply(BigDecimal.valueOf(result.getCredits())));

        totalCredits += result.getCredits();
      } else {
        complete = false;
      }

      earnedCredits += result.getEarnedCredits();

      courses.add(
          TranscriptCourseResponse.builder()
              .courseId(course.getId())
              .ref(course.getRef())
              .title(course.getTitle())
              .credits(course.getCredits())
              .finalGrade(result.getFinalGrade())
              .validated(validated)
              .build());
    }

    BigDecimal annualAverage = null;

    if (complete && totalCredits > 0) {
      annualAverage =
          weightedTotal.divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP);
    }

    return TranscriptResponse.builder()
        .studentId(studentId)
        .complete(complete)
        .annualAverage(annualAverage)
        .earnedCredits(earnedCredits)
        .courses(courses)
        .build();
  }
}
