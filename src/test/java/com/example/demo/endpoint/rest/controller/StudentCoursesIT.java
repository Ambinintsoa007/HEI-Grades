package com.example.demo.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.CourseManagementTestBase;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.StudentCourseEnrollmentRepository;
import com.example.demo.repository.model.CourseOfferingEntity;
import com.example.demo.repository.model.StudentCourseEnrollmentEntity;
import com.example.demo.repository.model.UserEntity;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class StudentCoursesIT extends CourseManagementTestBase {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private CourseOfferingRepository courseOfferingRepository;
  @Autowired private StudentCourseEnrollmentRepository studentCourseEnrollmentRepository;

  private UserEntity student;
  private String courseRef;
  private String enrollmentId;
  private String coursesUrl;

  @BeforeEach
  void setUp() {
    student = saveStudent();
    var course = saveCourse("REG-C-" + UUID.randomUUID().toString().substring(0, 8), 5);
    courseRef = course.getRef();
    var year = saveAcademicYear("REG-AY-" + UUID.randomUUID().toString().substring(0, 8));
    var group = saveGroup("REG-G-" + UUID.randomUUID().toString().substring(0, 8));
    var offering =
        courseOfferingRepository.save(
            CourseOfferingEntity.builder()
                .id(UUID.randomUUID())
                .course(course)
                .academicYear(year)
                .group(group)
                .build());
    var enrollment =
        studentCourseEnrollmentRepository.save(
            StudentCourseEnrollmentEntity.builder()
                .id(UUID.randomUUID())
                .student(student)
                .course(course)
                .academicYear(year)
                .enrolledAt(Instant.parse("2024-10-01T00:00:00Z"))
                .courseOfferings(Set.of(offering))
                .build());
    enrollmentId = enrollment.getId().toString();
    coursesUrl = "/students/" + student.getId() + "/courses";
  }

  @Test
  void studentWithoutEnrollmentReturnsEmptyList() {
    var emptyStudent = saveStudent();

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/students/" + emptyStudent.getId() + "/courses",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(saveAdmin())),
            String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode(), response.getBody());
    assertEquals("[]", response.getBody());
  }

  @Test
  void studentWithEnrollmentAndCourseOfferingsReturnsCourses() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            coursesUrl, HttpMethod.GET, new HttpEntity<>(authHeaders(saveAdmin())), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode(), response.getBody());
    String body = response.getBody();
    assertTrue(body.contains("\"enrollmentId\""), body);
    assertTrue(body.contains("\"courseId\""), body);
    assertTrue(body.contains("\"ref\""), body);
    assertTrue(body.contains("\"title\""), body);
    assertTrue(body.contains("\"credits\""), body);
    assertTrue(body.contains("\"academicYearId\""), body);
    assertTrue(body.contains("\"enrolledAt\""), body);
    assertTrue(body.contains(enrollmentId), body);
    assertTrue(body.contains(courseRef), body);
  }
}
