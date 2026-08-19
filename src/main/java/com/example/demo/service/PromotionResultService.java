package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.GraduateResponse;
import com.example.demo.endpoint.rest.dto.PromotionResultsResponse;
import com.example.demo.endpoint.rest.dto.PromotionStudentResultResponse;
import com.example.demo.endpoint.rest.dto.StudentAcademicYearResultResponse;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.model.Pathway;
import com.example.demo.model.StudentCourseResult;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.StudentCourseEnrollmentRepository;
import com.example.demo.repository.StudentGroupHistoryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.PromotionEntity;
import com.example.demo.repository.model.StudentGroupHistoryEntity;
import com.example.demo.repository.model.UserEntity;
import com.example.demo.repository.model.UserRoleEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PromotionResultService {

  private static final BigDecimal PASSING_GRADE = new BigDecimal("10");
  private static final int AVERAGE_SCALE = 2;

  private final PromotionRepository promotionRepository;
  private final UserRepository userRepository;
  private final StudentCourseEnrollmentRepository studentCourseEnrollmentRepository;
  private final AcademicYearRepository academicYearRepository;
  private final StudentGroupHistoryRepository studentGroupHistoryRepository;
  private final StudentCourseResultService studentCourseResultService;

  @Transactional(readOnly = true)
  public PromotionResultsResponse getPromotionResults(UUID promotionId) {
    requireExistingPromotion(promotionId);
    List<PromotionStudentResultResponse> students =
        promotionStudents(promotionId).stream()
            .map(student -> toStudentResult(student, studentResults(student)))
            .toList();
    return PromotionResultsResponse.builder().promotionId(promotionId).students(students).build();
  }

  @Transactional(readOnly = true)
  public List<GraduateResponse> getGraduates(UUID promotionId) {
    requireExistingPromotion(promotionId);
    return promotionStudents(promotionId).stream()
        .map(student -> Map.entry(student, studentResults(student)))
        .filter(entry -> isGraduate(entry.getValue()))
        .map(entry -> toGraduateResponse(entry.getKey(), entry.getValue()))
        .toList();
  }

  private PromotionEntity requireExistingPromotion(UUID promotionId) {
    return promotionRepository
        .findById(promotionId)
        .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
  }

  private List<UserEntity> promotionStudents(UUID promotionId) {
    return userRepository.findByRoleAndPromotion_Id(UserRoleEntity.STUDENT, promotionId);
  }

  private List<StudentCourseResult> studentResults(UserEntity student) {
    return studentCourseEnrollmentRepository.findAllByStudent_Id(student.getId()).stream()
        .map(enrollment -> studentCourseResultService.calculate(enrollment.getId()))
        .toList();
  }

  private PromotionStudentResultResponse toStudentResult(
      UserEntity student, List<StudentCourseResult> results) {
    List<StudentAcademicYearResultResponse> academicYears =
        results.stream()
            .collect(Collectors.groupingBy(StudentCourseResult::getAcademicYearId))
            .entrySet()
            .stream()
            .map(this::toAcademicYearResult)
            .sorted(Comparator.comparing(StudentAcademicYearResultResponse::getAcademicYearLabel))
            .toList();

    return PromotionStudentResultResponse.builder()
        .studentId(student.getId())
        .std(student.getStd())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .complete(isComplete(results))
        .graduate(isGraduate(results))
        .academicYears(academicYears)
        .build();
  }

  private StudentAcademicYearResultResponse toAcademicYearResult(
      Map.Entry<UUID, List<StudentCourseResult>> entry) {
    UUID academicYearId = entry.getKey();
    List<StudentCourseResult> results = entry.getValue();

    boolean complete = results.stream().allMatch(StudentCourseResult::isComplete);
    int earnedCredits = results.stream().mapToInt(StudentCourseResult::getEarnedCredits).sum();
    BigDecimal average = complete ? weightedAverage(results) : null;

    return StudentAcademicYearResultResponse.builder()
        .academicYearId(academicYearId)
        .academicYearLabel(academicYearLabel(academicYearId))
        .average(average)
        .earnedCredits(earnedCredits)
        .complete(complete)
        .build();
  }

  private boolean hasFullThreeYearProgram(List<StudentCourseResult> results) {
    return results.stream().map(StudentCourseResult::getAcademicYearId).distinct().count() == 3;
  }

  private boolean isComplete(List<StudentCourseResult> results) {
    return hasFullThreeYearProgram(results)
        && results.stream().allMatch(StudentCourseResult::isComplete);
  }

  private boolean isGraduate(List<StudentCourseResult> results) {
    return isComplete(results)
        && results.stream()
            .allMatch(result -> result.getFinalGrade().compareTo(PASSING_GRADE) >= 0);
  }

  private BigDecimal weightedAverage(List<StudentCourseResult> results) {
    BigDecimal weightedTotal = BigDecimal.ZERO;
    BigDecimal totalCredits = BigDecimal.ZERO;
    for (StudentCourseResult result : results) {
      BigDecimal credits = BigDecimal.valueOf(result.getCredits());
      weightedTotal = weightedTotal.add(result.getFinalGrade().multiply(credits));
      totalCredits = totalCredits.add(credits);
    }
    if (totalCredits.signum() == 0) {
      return null;
    }
    return weightedTotal.divide(totalCredits, AVERAGE_SCALE, RoundingMode.HALF_UP);
  }

  private String academicYearLabel(UUID academicYearId) {
    return academicYearRepository
        .findById(academicYearId)
        .map(academicYear -> academicYear.getLabel())
        .orElse(null);
  }

  private GraduateResponse toGraduateResponse(
      UserEntity student, List<StudentCourseResult> results) {
    return GraduateResponse.builder()
        .studentId(student.getId())
        .std(student.getStd())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .pathway(currentPathway(student.getId()))
        .overallAverage(weightedAverage(results))
        .build();
  }

  private Pathway currentPathway(UUID studentId) {
    return studentGroupHistoryRepository
        .findByStudent_IdAndEndedAtIsNull(studentId)
        .or(() -> lastHistory(studentId))
        .map(StudentGroupHistoryEntity::getPathway)
        .map(pathway -> Pathway.valueOf(pathway.name()))
        .orElse(null);
  }

  private Optional<StudentGroupHistoryEntity> lastHistory(UUID studentId) {
    List<StudentGroupHistoryEntity> histories =
        studentGroupHistoryRepository.findByStudent_IdOrderByStartedAtAsc(studentId);
    return histories.isEmpty()
        ? Optional.empty()
        : Optional.of(histories.get(histories.size() - 1));
  }
}
