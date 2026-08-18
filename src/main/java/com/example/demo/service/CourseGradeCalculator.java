package com.example.demo.service;

import com.example.demo.model.CourseGradeResult;
import com.example.demo.model.ExamGrade;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CourseGradeCalculator {

  public CourseGradeResult calculate(List<ExamGrade> exams) {
    if (exams == null || exams.isEmpty()) {
      return incomplete();
    }

    if (exams.stream().anyMatch(exam -> exam.getScore() == null)) {
      return incomplete();
    }

    BigDecimal totalCoefficient = BigDecimal.ZERO;
    BigDecimal weightedTotal = BigDecimal.ZERO;

    for (ExamGrade exam : exams) {
      totalCoefficient = totalCoefficient.add(exam.getCoefficient());

      weightedTotal = weightedTotal.add(exam.getScore().multiply(exam.getCoefficient()));
    }

    if (totalCoefficient.compareTo(BigDecimal.ZERO) <= 0) {
      return incomplete();
    }

    BigDecimal finalGrade = weightedTotal.divide(totalCoefficient, 2, RoundingMode.HALF_UP);

    return CourseGradeResult.builder().complete(true).finalGrade(finalGrade).build();
  }

  private CourseGradeResult incomplete() {
    return CourseGradeResult.builder().complete(false).finalGrade(null).build();
  }
}
