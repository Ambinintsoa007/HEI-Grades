package com.example.demo.endpoint.rest.dto;

import com.example.demo.model.Exam;
import java.math.BigDecimal;
import java.util.UUID;

public record ExamResponse(UUID id, String ref, UUID courseOfferingId, BigDecimal coefficient) {

  public static ExamResponse from(Exam exam) {
    return new ExamResponse(
        exam.getId(), exam.getRef(), exam.getCourseOfferingId(), exam.getCoefficient());
  }
}
