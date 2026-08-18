package com.example.demo.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.demo.CourseManagementTestBase;
import com.example.demo.endpoint.rest.dto.CourseOfferingResponse;
import com.example.demo.endpoint.rest.dto.ExamResponse;
import com.example.demo.repository.model.UserEntity;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ExamControllerIT extends CourseManagementTestBase {

  @Autowired private TestRestTemplate restTemplate;

  private UUID createOffering(UserEntity admin, String suffix) {
    var course = saveCourse("CRS-EXC-" + suffix, 5);
    var year = saveAcademicYear("AY-EXC-" + suffix);
    var group = saveGroup("G-EXC-" + suffix);
    var body =
        Map.of(
            "courseId", course.getId().toString(),
            "academicYearId", year.getId().toString(),
            "groupId", group.getId().toString());
    return restTemplate
        .exchange(
            "/course-offerings",
            HttpMethod.POST,
            new HttpEntity<>(body, authHeaders(admin)),
            CourseOfferingResponse.class)
        .getBody()
        .id();
  }

  private ResponseEntity<ExamResponse> createExamAs(
      UserEntity user, UUID offeringId, Map<?, ?> body) {
    return restTemplate.exchange(
        "/course-offerings/" + offeringId + "/exams",
        HttpMethod.POST,
        new HttpEntity<>(body, authHeaders(user)),
        ExamResponse.class);
  }

  @Test
  void admin_can_create_exam_returns_201() {
    var admin = saveAdmin();
    var offeringId = createOffering(admin, "1");

    var response = createExamAs(admin, offeringId, Map.of("ref", "DS", "coefficient", 2));

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("DS", response.getBody().ref());
    assertEquals(offeringId, response.getBody().courseOfferingId());
    assertEquals(0, new BigDecimal("2").compareTo(response.getBody().coefficient()));
  }

  @Test
  void create_exam_rejects_non_positive_coefficient_returns_400() {
    var admin = saveAdmin();
    var offeringId = createOffering(admin, "2");

    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/course-offerings/" + offeringId + "/exams",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("ref", "DS", "coefficient", 0), authHeaders(admin)),
            Void.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void create_exam_rejects_duplicate_ref_returns_409() {
    var admin = saveAdmin();
    var offeringId = createOffering(admin, "3");
    var body = Map.of("ref", "DS", "coefficient", 2);
    createExamAs(admin, offeringId, body);

    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/course-offerings/" + offeringId + "/exams",
            HttpMethod.POST,
            new HttpEntity<>(body, authHeaders(admin)),
            Void.class);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
  }

  @Test
  void admin_lists_exams_returns_200() {
    var admin = saveAdmin();
    var offeringId = createOffering(admin, "4");
    createExamAs(admin, offeringId, Map.of("ref", "DS", "coefficient", 2));
    createExamAs(admin, offeringId, Map.of("ref", "Final", "coefficient", 3));

    ResponseEntity<ExamResponse[]> response =
        restTemplate.exchange(
            "/course-offerings/" + offeringId + "/exams",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(admin)),
            ExamResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(2, response.getBody().length);
  }

  @Test
  void list_exams_unknown_offering_returns_404() {
    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/course-offerings/" + UUID.randomUUID() + "/exams",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(saveAdmin())),
            Void.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void assigned_teacher_can_create_and_list_exams() {
    var admin = saveAdmin();
    var teacher = saveTeacher();
    var offeringId = createOffering(admin, "5");
    restTemplate.exchange(
        "/course-offerings/" + offeringId + "/teachers",
        HttpMethod.POST,
        new HttpEntity<>(Map.of("teacherId", teacher.getId().toString()), authHeaders(admin)),
        Void.class);

    var createResponse = createExamAs(teacher, offeringId, Map.of("ref", "DS", "coefficient", 2));
    ResponseEntity<ExamResponse[]> listResponse =
        restTemplate.exchange(
            "/course-offerings/" + offeringId + "/exams",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(teacher)),
            ExamResponse[].class);

    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
    assertEquals(HttpStatus.OK, listResponse.getStatusCode());
    assertEquals(1, listResponse.getBody().length);
  }

  @Test
  void unassigned_teacher_cannot_access_exams_returns_403() {
    var admin = saveAdmin();
    var teacher = saveTeacher();
    var offeringId = createOffering(admin, "6");

    ResponseEntity<Void> getResponse =
        restTemplate.exchange(
            "/course-offerings/" + offeringId + "/exams",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(teacher)),
            Void.class);
    ResponseEntity<Void> postResponse =
        restTemplate.exchange(
            "/course-offerings/" + offeringId + "/exams",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("ref", "DS", "coefficient", 2), authHeaders(teacher)),
            Void.class);

    assertEquals(HttpStatus.FORBIDDEN, getResponse.getStatusCode());
    assertEquals(HttpStatus.FORBIDDEN, postResponse.getStatusCode());
  }

  @Test
  void student_cannot_access_exams_returns_403() {
    var admin = saveAdmin();
    var student = saveStudent();
    var offeringId = createOffering(admin, "7");

    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/course-offerings/" + offeringId + "/exams",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(student)),
            Void.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }
}
