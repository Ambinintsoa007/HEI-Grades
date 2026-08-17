package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.CourseManagementTestBase;
import com.example.demo.endpoint.rest.dto.CreateCourseOfferingRequest;
import com.example.demo.endpoint.rest.exception.ConflictException;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.model.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CourseOfferingServiceIT extends CourseManagementTestBase {

  @Autowired private CourseOfferingService courseOfferingService;
  @Autowired private TeacherAssignmentService teacherAssignmentService;

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
        ConflictException.class, () -> courseOfferingService.createCourseOffering(request));
  }

  @Test
  void create_offering_rejects_unknown_course_year_group() {
    var course = saveCourse("CRS-OF-3", 5);
    var year = saveAcademicYear("AY-OF-3");
    var group = saveGroup("G-OF-3");

    assertThrows(
        ResourceNotFoundException.class,
        () ->
            courseOfferingService.createCourseOffering(
                new CreateCourseOfferingRequest(UUID.randomUUID(), year.getId(), group.getId())));
    assertThrows(
        ResourceNotFoundException.class,
        () ->
            courseOfferingService.createCourseOffering(
                new CreateCourseOfferingRequest(course.getId(), UUID.randomUUID(), group.getId())));
    assertThrows(
        ResourceNotFoundException.class,
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
    var adminId = UUID.randomUUID();

    assertTrue(
        courseOfferingService.listCourseOfferings(adminId, UserRole.ADMIN, null, null).stream()
            .anyMatch(offering -> offering.getId().equals(offering11.getId())));

    var byYear =
        courseOfferingService.listCourseOfferings(adminId, UserRole.ADMIN, year1.getId(), null);
    assertEquals(2, byYear.size());
    assertTrue(byYear.stream().allMatch(o -> o.getAcademicYearId().equals(year1.getId())));

    var byGroup =
        courseOfferingService.listCourseOfferings(adminId, UserRole.ADMIN, null, group1.getId());
    assertEquals(2, byGroup.size());
    assertTrue(byGroup.stream().allMatch(o -> o.getGroupId().equals(group1.getId())));

    var byBoth =
        courseOfferingService.listCourseOfferings(
            adminId, UserRole.ADMIN, year1.getId(), group1.getId());
    assertEquals(1, byBoth.size());
    assertEquals(offering11.getId(), byBoth.get(0).getId());
  }

  @Test
  void list_offerings_filters_for_teacher() {
    var course = saveCourse("CRS-OF-5", 5);
    var year = saveAcademicYear("AY-OF-5");
    var group1 = saveGroup("G-OF-5A");
    var group2 = saveGroup("G-OF-5B");
    var offering1 =
        courseOfferingService.createCourseOffering(
            new CreateCourseOfferingRequest(course.getId(), year.getId(), group1.getId()));
    var offering2 =
        courseOfferingService.createCourseOffering(
            new CreateCourseOfferingRequest(course.getId(), year.getId(), group2.getId()));
    var teacher = saveTeacher();
    teacherAssignmentService.assignTeacher(offering1.getId(), teacher.getId());
    var otherTeacherId = UUID.randomUUID();

    var teacherList =
        courseOfferingService.listCourseOfferings(teacher.getId(), UserRole.TEACHER, null, null);
    assertEquals(1, teacherList.size());
    assertEquals(offering1.getId(), teacherList.get(0).getId());

    var teacherListByYear =
        courseOfferingService.listCourseOfferings(
            teacher.getId(), UserRole.TEACHER, year.getId(), null);
    assertEquals(1, teacherListByYear.size());
    assertEquals(offering1.getId(), teacherListByYear.get(0).getId());

    var teacherListByGroup =
        courseOfferingService.listCourseOfferings(
            teacher.getId(), UserRole.TEACHER, null, group2.getId());
    assertEquals(0, teacherListByGroup.size());

    var unassignedTeacherList =
        courseOfferingService.listCourseOfferings(otherTeacherId, UserRole.TEACHER, null, null);
    assertEquals(0, unassignedTeacherList.size());
  }
}
