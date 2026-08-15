package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.CreateExamRequest;
import com.example.demo.endpoint.rest.dto.ExamResponse;
import com.example.demo.service.ExamService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ExamController extends ApiExceptionHandler {

  private final ExamService examService;

  @GetMapping("/course-offerings/{courseOfferingId}/exams")
  public List<ExamResponse> getExams(@PathVariable UUID courseOfferingId) {
    return examService.listExams(courseOfferingId).stream().map(ExamResponse::from).toList();
  }

  @PostMapping("/course-offerings/{courseOfferingId}/exams")
  public ResponseEntity<ExamResponse> createExam(
      @PathVariable UUID courseOfferingId, @RequestBody CreateExamRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ExamResponse.from(examService.createExam(courseOfferingId, request)));
  }
}
