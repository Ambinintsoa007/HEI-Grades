package com.example.demo.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.demo.CourseManagementTestBase;
import com.example.demo.endpoint.rest.dto.CourseOfferingResponse;
import com.example.demo.repository.model.UserEntity;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CourseOfferingControllerIT extends CourseManagementTestBase {

  @Autowired private TestRestTemplate restTemplate;

  private Map<String, String> offeringBody(UUID courseId, UUID yearId, UUID groupId) {
    return Map.of(
        "courseId", courseId.toString(),
        "academicYearId", yearId.toString(),
        "groupId", groupId.toString());
  }

  private ResponseEntity<CourseOfferingResponse> createOffering(
      UserEntity user, UUID courseId, UUID yearId, UUID groupId) {
    return restTemplate.exchange(
        "/course-offerings",
        HttpMethod.POST,
        new HttpEntity<>(offeringBody(courseId, yearId, groupId), authHeaders(user)),
        CourseOfferingResponse.class);
  }

  @Test
  void admin_can_create_offering_returns_201() {
    var course = saveCourse("CRS-OFC-1", 5);
    var year = saveAcademicYear("AY-OFC-1");
    var group = saveGroup("G-OFC-1");

    var response = createOffering(saveAdmin(), course.getId(), year.getId(), group.getId());

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().id());
    assertEquals(course.getId(), response.getBody().courseId());
    assertEquals(year.getId(), response.getBody().academicYearId());
    assertEquals(group.getId(), response.getBody().groupId());
  }

  @Test
  void create_duplicate_offering_returns_409() {
    var course = saveCourse("CRS-OFC-2", 5);
    var year = saveAcademicYear("AY-OFC-2");
    var group = saveGroup("G-OFC-2");
    var admin = saveAdmin();
    createOffering(admin, course.getId(), year.getId(), group.getId());

    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/course-offerings",
            HttpMethod.POST,
            new HttpEntity<>(
                offeringBody(course.getId(), year.getId(), group.getId()), authHeaders(admin)),
            Void.class);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
  }

  @Test
  void admin_lists_offerings_with_year_and_group_filters() {
    var course = saveCourse("CRS-OFC-3", 5);
    var year1 = saveAcademicYear("AY-OFC-3A");
    var year2 = saveAcademicYear("AY-OFC-3B");
    var group1 = saveGroup("G-OFC-3A");
    var group2 = saveGroup("G-OFC-3B");
    var admin = saveAdmin();
    var offering11 = createOffering(admin, course.getId(), year1.getId(), group1.getId()).getBody();
    createOffering(admin, course.getId(), year1.getId(), group2.getId());
    createOffering(admin, course.getId(), year2.getId(), group1.getId());

    ResponseEntity<CourseOfferingResponse[]> response =
        restTemplate.exchange(
            "/course-offerings?academicYearId=" + year1.getId() + "&groupId=" + group1.getId(),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(admin)),
            CourseOfferingResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().length);
    assertEquals(offering11.id(), response.getBody()[0].id());
  }

  @Test
  void admin_can_assign_teacher_returns_201() {
    var course = saveCourse("CRS-OFC-4", 5);
    var year = saveAcademicYear("AY-OFC-4");
    var group = saveGroup("G-OFC-4");
    var admin = saveAdmin();
    var offering = createOffering(admin, course.getId(), year.getId(), group.getId()).getBody();
    var teacher = saveTeacher();

    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/course-offerings/" + offering.id() + "/teachers",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("teacherId", teacher.getId().toString()), authHeaders(admin)),
            Void.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  void assign_student_as_teacher_returns_400() {
    var course = saveCourse("CRS-OFC-5", 5);
    var year = saveAcademicYear("AY-OFC-5");
    var group = saveGroup("G-OFC-5");
    var admin = saveAdmin();
    var offering = createOffering(admin, course.getId(), year.getId(), group.getId()).getBody();
    var student = saveStudent();

    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/course-offerings/" + offering.id() + "/teachers",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("teacherId", student.getId().toString()), authHeaders(admin)),
            Void.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void teacher_sees_only_assigned_offerings() {
    var course = saveCourse("CRS-OFC-6", 5);
    var year1 = saveAcademicYear("AY-OFC-6A");
    var year2 = saveAcademicYear("AY-OFC-6B");
    var group1 = saveGroup("G-OFC-6A");
    var group2 = saveGroup("G-OFC-6B");
    var admin = saveAdmin();
    var teacher = saveTeacher();
    var assigned = createOffering(admin, course.getId(), year1.getId(), group1.getId()).getBody();
    createOffering(admin, course.getId(), year1.getId(), group2.getId());
    createOffering(admin, course.getId(), year2.getId(), group1.getId());
    restTemplate.exchange(
        "/course-offerings/" + assigned.id() + "/teachers",
        HttpMethod.POST,
        new HttpEntity<>(Map.of("teacherId", teacher.getId().toString()), authHeaders(admin)),
        Void.class);

    ResponseEntity<CourseOfferingResponse[]> response =
        restTemplate.exchange(
            "/course-offerings",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(teacher)),
            CourseOfferingResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().length);
    assertEquals(assigned.id(), response.getBody()[0].id());
  }

  @Test
  void teacher_cannot_create_offering_returns_403() {
    var course = saveCourse("CRS-OFC-7", 5);
    var year = saveAcademicYear("AY-OFC-7");
    var group = saveGroup("G-OFC-7");

    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/course-offerings",
            HttpMethod.POST,
            new HttpEntity<>(
                offeringBody(course.getId(), year.getId(), group.getId()),
                authHeaders(saveTeacher())),
            Void.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void teacher_cannot_assign_teacher_returns_403() {
    var course = saveCourse("CRS-OFC-8", 5);
    var year = saveAcademicYear("AY-OFC-8");
    var group = saveGroup("G-OFC-8");
    var admin = saveAdmin();
    var offering = createOffering(admin, course.getId(), year.getId(), group.getId()).getBody();

    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/course-offerings/" + offering.id() + "/teachers",
            HttpMethod.POST,
            new HttpEntity<>(
                Map.of("teacherId", UUID.randomUUID().toString()), authHeaders(saveTeacher())),
            Void.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void student_cannot_list_offerings_returns_403() {
    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/course-offerings",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(saveStudent())),
            Void.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }
}
