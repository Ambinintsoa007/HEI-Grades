package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.CourseManagementTestBase;
import com.example.demo.endpoint.rest.dto.CreateCourseRequest;
import com.example.demo.endpoint.rest.dto.UpdateCourseRequest;
import com.example.demo.endpoint.rest.exception.BusinessException;
import com.example.demo.endpoint.rest.exception.ConflictException;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CourseServiceIT extends CourseManagementTestBase {

  @Autowired private CourseService courseService;

  @Test
  void create_course_success() {
    var created = courseService.createCourse(new CreateCourseRequest("CRS-SVC-1", "Algo", 5));

    assertNotNull(created.getId());
    assertEquals("CRS-SVC-1", created.getRef());
    assertEquals("Algo", created.getTitle());
    assertEquals(5, created.getCredits());
  }

  @Test
  void create_course_rejects_credits_zero_or_negative() {
    assertThrows(
        BusinessException.class,
        () -> courseService.createCourse(new CreateCourseRequest("CRS-SVC-2", "Algo", 0)));
    assertThrows(
        BusinessException.class,
        () -> courseService.createCourse(new CreateCourseRequest("CRS-SVC-3", "Algo", -1)));
  }

  @Test
  void create_course_rejects_blank_ref() {
    assertThrows(
        BusinessException.class,
        () -> courseService.createCourse(new CreateCourseRequest("   ", "Algo", 5)));
  }

  @Test
  void create_course_rejects_blank_title() {
    assertThrows(
        BusinessException.class,
        () -> courseService.createCourse(new CreateCourseRequest("CRS-SVC-10", "   ", 5)));
  }

  @Test
  void create_course_rejects_duplicate_ref_case_insensitive() {
    courseService.createCourse(new CreateCourseRequest("CRS-SVC-4", "Algo", 5));

    assertThrows(
        ConflictException.class,
        () -> courseService.createCourse(new CreateCourseRequest("crs-svc-4", "Other", 3)));
  }

  @Test
  void list_courses_returns_created_course() {
    var created = courseService.createCourse(new CreateCourseRequest("CRS-SVC-5", "Réseaux", 4));

    var courses = courseService.listCourses();

    assertTrue(courses.stream().anyMatch(course -> course.getId().equals(created.getId())));
  }

  @Test
  void update_course_success() {
    var created = courseService.createCourse(new CreateCourseRequest("CRS-SVC-6", "Algo", 5));

    var updated =
        courseService.updateCourse(
            created.getId(), new UpdateCourseRequest("CRS-SVC-6B", "Algo avancé", 6));

    assertEquals(created.getId(), updated.getId());
    assertEquals("CRS-SVC-6B", updated.getRef());
    assertEquals("Algo avancé", updated.getTitle());
    assertEquals(6, updated.getCredits());
  }

  @Test
  void update_course_not_found() {
    assertThrows(
        ResourceNotFoundException.class,
        () ->
            courseService.updateCourse(
                UUID.randomUUID(), new UpdateCourseRequest("CRS-SVC-UNKNOWN", null, null)));
  }

  @Test
  void update_course_rejects_duplicate_ref() {
    courseService.createCourse(new CreateCourseRequest("CRS-SVC-7", "A", 5));
    var other = courseService.createCourse(new CreateCourseRequest("CRS-SVC-8", "B", 3));

    assertThrows(
        ConflictException.class,
        () ->
            courseService.updateCourse(
                other.getId(), new UpdateCourseRequest("crs-svc-7", null, null)));
  }

  @Test
  void update_course_rejects_non_positive_credits() {
    var created = courseService.createCourse(new CreateCourseRequest("CRS-SVC-9", "A", 5));

    assertThrows(
        BusinessException.class,
        () -> courseService.updateCourse(created.getId(), new UpdateCourseRequest(null, null, 0)));
  }
}
