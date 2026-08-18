package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.demo.CourseManagementTestBase;
import com.example.demo.endpoint.rest.dto.CreateCourseOfferingRequest;
import com.example.demo.endpoint.rest.dto.CreateExamRequest;
import com.example.demo.endpoint.rest.exception.BusinessException;
import com.example.demo.endpoint.rest.exception.ConflictException;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ExamServiceIT extends CourseManagementTestBase {

  @Autowired private ExamService examService;
  @Autowired private CourseOfferingService courseOfferingService;

  private UUID createOffering(String suffix) {
    var course = saveCourse("CRS-EX-" + suffix, 5);
    var year = saveAcademicYear("AY-EX-" + suffix);
    var group = saveGroup("G-EX-" + suffix);
    return courseOfferingService
        .createCourseOffering(
            new CreateCourseOfferingRequest(course.getId(), year.getId(), group.getId()))
        .getId();
  }

  @Test
  void create_exam_success() {
    var offeringId = createOffering("1");

    var exam = examService.createExam(offeringId, new CreateExamRequest("DS", new BigDecimal("2")));

    assertNotNull(exam.getId());
    assertEquals("DS", exam.getRef());
    assertEquals(offeringId, exam.getCourseOfferingId());
    assertEquals(new BigDecimal("2"), exam.getCoefficient());
  }

  @Test
  void create_exam_rejects_non_positive_coefficient() {
    var offeringId = createOffering("2");

    assertThrows(
        BusinessException.class,
        () -> examService.createExam(offeringId, new CreateExamRequest("DS", BigDecimal.ZERO)));
    assertThrows(
        BusinessException.class,
        () ->
            examService.createExam(offeringId, new CreateExamRequest("DS", new BigDecimal("-1"))));
  }

  @Test
  void create_exam_rejects_blank_ref() {
    var offeringId = createOffering("6");

    assertThrows(
        BusinessException.class,
        () -> examService.createExam(offeringId, new CreateExamRequest("   ", BigDecimal.ONE)));
  }

  @Test
  void create_exam_rejects_duplicate_ref_same_offering() {
    var offeringId = createOffering("3");

    examService.createExam(offeringId, new CreateExamRequest("DS", BigDecimal.ONE));

    assertThrows(
        ConflictException.class,
        () -> examService.createExam(offeringId, new CreateExamRequest("ds", BigDecimal.TEN)));
  }

  @Test
  void same_ref_allowed_on_different_offerings() {
    var offeringId1 = createOffering("4A");
    var offeringId2 = createOffering("4B");

    examService.createExam(offeringId1, new CreateExamRequest("DS", BigDecimal.ONE));
    var exam2 = examService.createExam(offeringId2, new CreateExamRequest("DS", BigDecimal.ONE));

    assertNotNull(exam2.getId());
  }

  @Test
  void list_exams_sorted_by_ref() {
    var offeringId = createOffering("5");

    examService.createExam(offeringId, new CreateExamRequest("Final", new BigDecimal("3")));
    examService.createExam(offeringId, new CreateExamRequest("DS", BigDecimal.ONE));

    var exams = examService.listExams(offeringId);

    assertEquals(2, exams.size());
    assertEquals("DS", exams.get(0).getRef());
    assertEquals("Final", exams.get(1).getRef());
  }

  @Test
  void list_exams_unknown_offering_refused() {
    assertThrows(ResourceNotFoundException.class, () -> examService.listExams(UUID.randomUUID()));
  }
}
