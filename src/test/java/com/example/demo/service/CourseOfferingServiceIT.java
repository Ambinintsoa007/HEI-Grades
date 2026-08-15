package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.CourseManagementTestBase;
import com.example.demo.endpoint.rest.dto.CreateCourseOfferingRequest;
import com.example.demo.service.exception.BusinessException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CourseOfferingServiceIT extends CourseManagementTestBase {

  @Autowired private CourseOfferingService courseOfferingService;

  @Test
  void create_offering_success() {
    var course = saveCourse("CRS-OF-1", 5);
    var year = saveAcademicYear("AY-OF-1");
    var group = saveGroup("G-OF-1");

    var offering =
        courseOfferingService.createCourseOffering(
            new CreateCourseOfferingRequest(course.getId(), year.getId(), group.getId()));

    assertNotNull(offering.getId());
    assertEquals(course.getId(), offering.getCourseId());
    assertEquals(year.getId(), offering.getAcademicYearId());
    assertEquals(group.getId(), offering.getGroupId());
  }

  @Test
  void create_offering_rejects_duplicate() {
    var course = saveCourse("CRS-OF-2", 5);
    var year = saveAcademicYear("AY-OF-2");
    var group = saveGroup("G-OF-2");
    var request = new CreateCourseOfferingRequest(course.getId(), year.getId(), group.getId());

    courseOfferingService.createCourseOffering(request);

    assertThrows(
        BusinessException.class, () -> courseOfferingService.createCourseOffering(request));
  }

  @Test
  void create_offering_rejects_unknown_course_year_group() {
    var course = saveCourse("CRS-OF-3", 5);
    var year = saveAcademicYear("AY-OF-3");
    var group = saveGroup("G-OF-3");

    assertThrows(
        BusinessException.class,
        () ->
            courseOfferingService.createCourseOffering(
                new CreateCourseOfferingRequest(UUID.randomUUID(), year.getId(), group.getId())));
    assertThrows(
        BusinessException.class,
        () ->
            courseOfferingService.createCourseOffering(
                new CreateCourseOfferingRequest(course.getId(), UUID.randomUUID(), group.getId())));
    assertThrows(
        BusinessException.class,
        () ->
            courseOfferingService.createCourseOffering(
                new CreateCourseOfferingRequest(course.getId(), year.getId(), UUID.randomUUID())));
  }

  @Test
  void list_offerings_filters() {
    var course = saveCourse("CRS-OF-4", 5);
    var year1 = saveAcademicYear("AY-OF-4A");
    var year2 = saveAcademicYear("AY-OF-4B");
    var group1 = saveGroup("G-OF-4A");
    var group2 = saveGroup("G-OF-4B");
    var offering11 =
        courseOfferingService.createCourseOffering(
            new CreateCourseOfferingRequest(course.getId(), year1.getId(), group1.getId()));
    courseOfferingService.createCourseOffering(
        new CreateCourseOfferingRequest(course.getId(), year1.getId(), group2.getId()));
    courseOfferingService.createCourseOffering(
        new CreateCourseOfferingRequest(course.getId(), year2.getId(), group1.getId()));

    assertTrue(
        courseOfferingService.listCourseOfferings(null, null).stream()
            .anyMatch(offering -> offering.getId().equals(offering11.getId())));

    var byYear = courseOfferingService.listCourseOfferings(year1.getId(), null);
    assertEquals(2, byYear.size());
    assertTrue(byYear.stream().allMatch(o -> o.getAcademicYearId().equals(year1.getId())));

    var byGroup = courseOfferingService.listCourseOfferings(null, group1.getId());
    assertEquals(2, byGroup.size());
    assertTrue(byGroup.stream().allMatch(o -> o.getGroupId().equals(group1.getId())));

    var byBoth = courseOfferingService.listCourseOfferings(year1.getId(), group1.getId());
    assertEquals(1, byBoth.size());
    assertEquals(offering11.getId(), byBoth.get(0).getId());
  }
}
