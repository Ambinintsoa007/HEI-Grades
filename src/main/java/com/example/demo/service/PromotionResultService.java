package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.AcademicYearResultResponse;
import com.example.demo.endpoint.rest.dto.GraduateResponse;
import com.example.demo.endpoint.rest.dto.PromotionStudentResultResponse;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.model.StudentCourseResult;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.StudentCourseEnrollmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.PromotionEntity;
import com.example.demo.repository.model.UserEntity;
import com.example.demo.repository.model.UserRoleEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
  private final StudentCourseResultService studentCourseResultService;

  @Transactional(readOnly = true)
  public List<PromotionStudentResultResponse> getPromotionResults(UUID promotionId) {
    requireExistingPromotion(promotionId);
    return promotionStudents(promotionId).stream().map(this::toStudentResult).toList();
  }

  @Transactional(readOnly = true)
  public List<GraduateResponse> getGraduates(UUID promotionId) {
    requireExistingPromotion(promotionId);
    return promotionStudents(promotionId).stream()
        .map(student -> Map.entry(student, toStudentResult(student)))
        .filter(entry -> entry.getValue().isGraduate())
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

  private PromotionStudentResultResponse toStudentResult(UserEntity student) {
    List<StudentCourseResult> results =
        studentCourseEnrollmentRepository.findAllByStudent_Id(student.getId()).stream()
            .map(enrollment -> studentCourseResultService.calculate(enrollment.getId()))
            .toList();

    boolean hasFullThreeYearProgram =
        results.stream().map(StudentCourseResult::getAcademicYearId).distinct().count() == 3;
    boolean complete =
        hasFullThreeYearProgram && results.stream().allMatch(StudentCourseResult::isComplete);
    boolean graduate =
        complete
            && results.stream()
                .allMatch(result -> result.getFinalGrade().compareTo(PASSING_GRADE) >= 0);
    int earnedCredits = results.stream().mapToInt(StudentCourseResult::getEarnedCredits).sum();
    int totalCredits = results.stream().mapToInt(StudentCourseResult::getCredits).sum();

    List<AcademicYearResultResponse> academicYears =
        results.stream()
            .collect(Collectors.groupingBy(StudentCourseResult::getAcademicYearId))
            .entrySet()
            .stream()
            .map(this::toAcademicYearResult)
            .sorted(Comparator.comparing(AcademicYearResultResponse::getLabel))
            .toList();

    return PromotionStudentResultResponse.builder()
        .studentId(student.getId())
        .std(student.getStd())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .complete(complete)
        .graduate(graduate)
        .earnedCredits(earnedCredits)
        .totalCredits(totalCredits)
        .academicYears(academicYears)
        .build();
  }

  private AcademicYearResultResponse toAcademicYearResult(
      Map.Entry<UUID, List<StudentCourseResult>> entry) {
    UUID academicYearId = entry.getKey();
    List<StudentCourseResult> results = entry.getValue();

    boolean complete = results.stream().allMatch(StudentCourseResult::isComplete);
    int earnedCredits = results.stream().mapToInt(StudentCourseResult::getEarnedCredits).sum();
    int totalCredits = results.stream().mapToInt(StudentCourseResult::getCredits).sum();
    BigDecimal average = complete ? weightedAverage(results) : null;

    return AcademicYearResultResponse.builder()
        .academicYearId(academicYearId)
        .label(academicYearLabel(academicYearId))
        .average(average)
        .earnedCredits(earnedCredits)
        .totalCredits(totalCredits)
        .complete(complete)
        .build();
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
      UserEntity student, PromotionStudentResultResponse result) {
    return GraduateResponse.builder()
        .studentId(student.getId())
        .std(student.getStd())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .email(student.getEmail())
        .earnedCredits(result.getEarnedCredits())
        .build();
  }
}
