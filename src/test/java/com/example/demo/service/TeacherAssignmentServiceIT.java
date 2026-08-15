package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.demo.CourseManagementTestBase;
import com.example.demo.endpoint.rest.dto.CreateCourseOfferingRequest;
import com.example.demo.repository.model.UserRoleEntity;
import com.example.demo.service.exception.BusinessException;
import com.example.demo.service.exception.ResourceNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TeacherAssignmentServiceIT extends CourseManagementTestBase {

  @Autowired private TeacherAssignmentService teacherAssignmentService;
  @Autowired private CourseOfferingService courseOfferingService;

  private UUID createOffering(String suffix) {
    var course = saveCourse("CRS-TA-" + suffix, 5);
    var year = saveAcademicYear("AY-TA-" + suffix);
    var group = saveGroup("G-TA-" + suffix);
    return courseOfferingService
        .createCourseOffering(
            new CreateCourseOfferingRequest(course.getId(), year.getId(), group.getId()))
        .getId();
  }

  @Test
  void assign_teacher_success() {
    var offeringId = createOffering("1");
    var teacher = saveUser("teacher1@test.com", UserRoleEntity.TEACHER);

    var assignment = teacherAssignmentService.assignTeacher(offeringId, teacher.getId());

    assertNotNull(assignment.getId());
    assertEquals(offeringId, assignment.getCourseOfferingId());
    assertEquals(teacher.getId(), assignment.getTeacherId());
  }

  @Test
  void assign_student_refused() {
    var offeringId = createOffering("2");
    var student = saveUser("student2@test.com", UserRoleEntity.STUDENT);

    assertThrows(
        BusinessException.class,
        () -> teacherAssignmentService.assignTeacher(offeringId, student.getId()));
  }

  @Test
  void assign_same_teacher_twice_refused() {
    var offeringId = createOffering("3");
    var teacher = saveUser("teacher3@test.com", UserRoleEntity.TEACHER);

    teacherAssignmentService.assignTeacher(offeringId, teacher.getId());

    assertThrows(
        BusinessException.class,
        () -> teacherAssignmentService.assignTeacher(offeringId, teacher.getId()));
  }

  @Test
  void two_teachers_can_share_offering() {
    var offeringId = createOffering("4");
    var first = saveUser("teacher4a@test.com", UserRoleEntity.TEACHER);
    var second = saveUser("teacher4b@test.com", UserRoleEntity.TEACHER);

    teacherAssignmentService.assignTeacher(offeringId, first.getId());
    var assignment = teacherAssignmentService.assignTeacher(offeringId, second.getId());

    assertNotNull(assignment.getId());
    assertEquals(second.getId(), assignment.getTeacherId());
  }

  @Test
  void assign_unknown_offering_refused() {
    var teacher = saveUser("teacher5@test.com", UserRoleEntity.TEACHER);

    assertThrows(
        ResourceNotFoundException.class,
        () -> teacherAssignmentService.assignTeacher(UUID.randomUUID(), teacher.getId()));
  }

  @Test
  void assign_unknown_teacher_refused() {
    var offeringId = createOffering("6");

    assertThrows(
        ResourceNotFoundException.class,
        () -> teacherAssignmentService.assignTeacher(offeringId, UUID.randomUUID()));
  }
}
