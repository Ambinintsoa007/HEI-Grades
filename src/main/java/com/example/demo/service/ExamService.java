package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.CreateExamRequest;
import com.example.demo.mapper.ExamMapper;
import com.example.demo.model.Exam;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.service.exception.BusinessException;
import com.example.demo.service.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ExamService {

  private final ExamRepository examRepository;
  private final CourseOfferingRepository courseOfferingRepository;
  private final ExamMapper examMapper;

  @Transactional(readOnly = true)
  public List<Exam> listExams(UUID courseOfferingId) {
    requireExistingOffering(courseOfferingId);
    return examRepository.findByCourseOffering_Id(courseOfferingId).stream()
        .map(examMapper::toDomain)
        .sorted(Comparator.comparing(Exam::getRef, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  @Transactional
  public Exam createExam(UUID courseOfferingId, CreateExamRequest request) {
    requireExistingOffering(courseOfferingId);
    if (request.ref() == null || request.ref().isBlank()) {
      throw new BusinessException("Exam ref must not be blank");
    }
    if (request.coefficient() == null || request.coefficient().compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessException("Exam coefficient must be greater than 0");
    }
    if (examRepository
        .findByCourseOffering_IdAndRefIgnoreCase(courseOfferingId, request.ref())
        .isPresent()) {
      throw new BusinessException(
          "Exam ref already exists for this course offering: " + request.ref());
    }
    Exam exam =
        Exam.builder()
            .id(UUID.randomUUID())
            .ref(request.ref())
            .courseOfferingId(courseOfferingId)
            .coefficient(request.coefficient())
            .build();
    return examMapper.toDomain(examRepository.save(examMapper.toEntity(exam)));
  }

  private void requireExistingOffering(UUID courseOfferingId) {
    courseOfferingRepository
        .findById(courseOfferingId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Course offering not found: " + courseOfferingId));
  }
}
