package com.example.demo.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.demo.CourseManagementTestBase;
import com.example.demo.endpoint.rest.dto.CourseOfferingResponse;
import com.example.demo.endpoint.rest.dto.ExamResponse;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ExamControllerIT extends CourseManagementTestBase {

  @Autowired private TestRestTemplate restTemplate;

  private UUID createOffering(String suffix) {
    var course = saveCourse("CRS-EXC-" + suffix, 5);
    var year = saveAcademicYear("AY-EXC-" + suffix);
    var group = saveGroup("G-EXC-" + suffix);
    var body =
        Map.of(
            "courseId", course.getId().toString(),
            "academicYearId", year.getId().toString(),
            "groupId", group.getId().toString());
    return restTemplate
        .postForEntity("/course-offerings", body, CourseOfferingResponse.class)
        .getBody()
        .id();
  }

  @Test
  void create_exam_returns_201() {
    var offeringId = createOffering("1");

    ResponseEntity<ExamResponse> response =
        restTemplate.postForEntity(
            "/course-offerings/" + offeringId + "/exams",
            Map.of("ref", "DS", "coefficient", 2),
            ExamResponse.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("DS", response.getBody().ref());
    assertEquals(offeringId, response.getBody().courseOfferingId());
    assertEquals(0, new java.math.BigDecimal("2").compareTo(response.getBody().coefficient()));
  }

  @Test
  void create_exam_rejects_non_positive_coefficient() {
    var offeringId = createOffering("2");

    ResponseEntity<Void> response =
        restTemplate.postForEntity(
            "/course-offerings/" + offeringId + "/exams",
            Map.of("ref", "DS", "coefficient", 0),
            Void.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void create_exam_rejects_duplicate_ref() {
    var offeringId = createOffering("3");
    var body = Map.of("ref", "DS", "coefficient", 2);
    restTemplate.postForEntity(
        "/course-offerings/" + offeringId + "/exams", body, ExamResponse.class);

    ResponseEntity<Void> response =
        restTemplate.postForEntity("/course-offerings/" + offeringId + "/exams", body, Void.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void list_exams_returns_200() {
    var offeringId = createOffering("4");
    restTemplate.postForEntity(
        "/course-offerings/" + offeringId + "/exams",
        Map.of("ref", "DS", "coefficient", 2),
        ExamResponse.class);
    restTemplate.postForEntity(
        "/course-offerings/" + offeringId + "/exams",
        Map.of("ref", "Final", "coefficient", 3),
        ExamResponse.class);

    ResponseEntity<ExamResponse[]> response =
        restTemplate.getForEntity(
            "/course-offerings/" + offeringId + "/exams", ExamResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(2, response.getBody().length);
  }

  @Test
  void list_exams_unknown_offering_returns_404() {
    ResponseEntity<Void> response =
        restTemplate.getForEntity("/course-offerings/" + UUID.randomUUID() + "/exams", Void.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }
}
