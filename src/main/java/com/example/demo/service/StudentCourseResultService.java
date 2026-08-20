package com.example.demo.service;

import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.model.ExamGrade;
import com.example.demo.model.StudentCourseResult;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeRepository;
import com.example.demo.repository.StudentCourseEnrollmentRepository;
import com.example.demo.repository.model.CourseOfferingEntity;
import com.example.demo.repository.model.ExamEntity;
import com.example.demo.repository.model.GradeEntity;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentCourseResultService {

  private static final BigDecimal PASSING_GRADE = new BigDecimal("10");

  private final StudentCourseEnrollmentRepository studentCourseEnrollmentRepository;
  private final ExamRepository examRepository;
  private final GradeRepository gradeRepository;
  private final CourseGradeCalculator courseGradeCalculator;

  @Transactional(readOnly = true)
  public StudentCourseResult calculate(UUID enrollmentId) {
    var enrollment =
        studentCourseEnrollmentRepository
            .findById(enrollmentId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Student course enrollment not found"));

    var offeringIds =
        enrollment.getCourseOfferings().stream().map(CourseOfferingEntity::getId).toList();
    var examsById = new LinkedHashMap<UUID, ExamEntity>();
    examRepository
        .findByCourseOffering_IdIn(offeringIds)
        .forEach(exam -> examsById.putIfAbsent(exam.getId(), exam));

    var scoresByExamId =
        gradeRepository.findByStudentCourseEnrollment_Id(enrollmentId).stream()
            .collect(
                Collectors.toMap(
                    grade -> grade.getExam().getId(),
                    GradeEntity::getScore,
                    (first, second) -> first));

    List<ExamGrade> examGrades =
        examsById.values().stream()
            .map(
                exam ->
                    ExamGrade.builder()
                        .coefficient(exam.getCoefficient())
                        .score(scoresByExamId.get(exam.getId()))
                        .build())
            .toList();

    var calculation = courseGradeCalculator.calculate(examGrades);

    int credits = enrollment.getCourse().getCredits();

    int earnedCredits =
        calculation.isComplete() && calculation.getFinalGrade().compareTo(PASSING_GRADE) >= 0
            ? credits
            : 0;

    return StudentCourseResult.builder()
        .enrollmentId(enrollment.getId())
        .courseId(enrollment.getCourse().getId())
        .academicYearId(enrollment.getAcademicYear().getId())
        .complete(calculation.isComplete())
        .finalGrade(calculation.getFinalGrade())
        .credits(credits)
        .earnedCredits(earnedCredits)
        .build();
  }
}
