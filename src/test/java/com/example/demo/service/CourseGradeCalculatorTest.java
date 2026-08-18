package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.model.ExamGrade;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CourseGradeCalculatorTest {

  private final CourseGradeCalculator calculator = new CourseGradeCalculator();

  @Test
  void shouldCalculateWeightedCourseGrade() {
    var result =
        calculator.calculate(
            List.of(
                ExamGrade.builder()
                    .score(new BigDecimal("12"))
                    .coefficient(new BigDecimal("1"))
                    .build(),
                ExamGrade.builder()
                    .score(new BigDecimal("16"))
                    .coefficient(new BigDecimal("2"))
                    .build()));

    assertTrue(result.isComplete());
    assertEquals(new BigDecimal("14.67"), result.getFinalGrade());
  }

  @Test
  void shouldBeIncompleteWhenGradeIsMissing() {
    var result =
        calculator.calculate(
            List.of(
                ExamGrade.builder().score(new BigDecimal("15")).coefficient(BigDecimal.ONE).build(),
                ExamGrade.builder().score(null).coefficient(BigDecimal.ONE).build()));

    assertFalse(result.isComplete());
    assertNull(result.getFinalGrade());
  }

  @Test
  void shouldBeIncompleteWhenThereAreNoExams() {
    var result = calculator.calculate(List.of());

    assertFalse(result.isComplete());
    assertNull(result.getFinalGrade());
  }
}
