package com.example.demo.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.demo.CourseManagementTestBase;
import com.example.demo.endpoint.rest.dto.CourseOfferingResponse;
import com.example.demo.repository.model.UserRoleEntity;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
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

  @Test
  void create_offering_returns_201() {
    var course = saveCourse("CRS-OFC-1", 5);
    var year = saveAcademicYear("AY-OFC-1");
    var group = saveGroup("G-OFC-1");

    ResponseEntity<CourseOfferingResponse> response =
        restTemplate.postForEntity(
            "/course-offerings",
            offeringBody(course.getId(), year.getId(), group.getId()),
            CourseOfferingResponse.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().id());
    assertEquals(course.getId(), response.getBody().courseId());
    assertEquals(year.getId(), response.getBody().academicYearId());
    assertEquals(group.getId(), response.getBody().groupId());
  }

  @Test
  void create_duplicate_offering_returns_400() {
    var course = saveCourse("CRS-OFC-2", 5);
    var year = saveAcademicYear("AY-OFC-2");
    var group = saveGroup("G-OFC-2");
    var body = offeringBody(course.getId(), year.getId(), group.getId());
    restTemplate.postForEntity("/course-offerings", body, CourseOfferingResponse.class);

    ResponseEntity<Void> response =
        restTemplate.postForEntity("/course-offerings", body, Void.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void list_offerings_filters_by_year_and_group() {
    var course = saveCourse("CRS-OFC-3", 5);
    var year1 = saveAcademicYear("AY-OFC-3A");
    var year2 = saveAcademicYear("AY-OFC-3B");
    var group1 = saveGroup("G-OFC-3A");
    var group2 = saveGroup("G-OFC-3B");
    var offering11 =
        restTemplate
            .postForEntity(
                "/course-offerings",
                offeringBody(course.getId(), year1.getId(), group1.getId()),
                CourseOfferingResponse.class)
            .getBody();
    restTemplate.postForEntity(
        "/course-offerings",
        offeringBody(course.getId(), year1.getId(), group2.getId()),
        CourseOfferingResponse.class);
    restTemplate.postForEntity(
        "/course-offerings",
        offeringBody(course.getId(), year2.getId(), group1.getId()),
        CourseOfferingResponse.class);

    ResponseEntity<CourseOfferingResponse[]> response =
        restTemplate.getForEntity(
            "/course-offerings?academicYearId=" + year1.getId() + "&groupId=" + group1.getId(),
            CourseOfferingResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().length);
    assertEquals(offering11.id(), response.getBody()[0].id());
  }

  @Test
  void assign_teacher_returns_201() {
    var course = saveCourse("CRS-OFC-4", 5);
    var year = saveAcademicYear("AY-OFC-4");
    var group = saveGroup("G-OFC-4");
    var offering =
        restTemplate
            .postForEntity(
                "/course-offerings",
                offeringBody(course.getId(), year.getId(), group.getId()),
                CourseOfferingResponse.class)
            .getBody();
    var teacher = saveUser("ofc-teacher4@test.com", UserRoleEntity.TEACHER);

    ResponseEntity<Void> response =
        restTemplate.postForEntity(
            "/course-offerings/" + offering.id() + "/teachers",
            Map.of("teacherId", teacher.getId().toString()),
            Void.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  void assign_student_as_teacher_returns_400() {
    var course = saveCourse("CRS-OFC-5", 5);
    var year = saveAcademicYear("AY-OFC-5");
    var group = saveGroup("G-OFC-5");
    var offering =
        restTemplate
            .postForEntity(
                "/course-offerings",
                offeringBody(course.getId(), year.getId(), group.getId()),
                CourseOfferingResponse.class)
            .getBody();
    var student = saveUser("ofc-student5@test.com", UserRoleEntity.STUDENT);

    ResponseEntity<Void> response =
        restTemplate.postForEntity(
            "/course-offerings/" + offering.id() + "/teachers",
            Map.of("teacherId", student.getId().toString()),
            Void.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }
}
