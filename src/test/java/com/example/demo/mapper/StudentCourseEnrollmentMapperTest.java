package com.example.demo.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.model.StudentCourseEnrollment;
import com.example.demo.repository.model.AcademicYearEntity;
import com.example.demo.repository.model.CourseEntity;
import com.example.demo.repository.model.StudentCourseEnrollmentEntity;
import com.example.demo.repository.model.UserEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StudentCourseEnrollmentMapperTest {

  private final StudentCourseEnrollmentMapper mapper = new StudentCourseEnrollmentMapper();

  @Test
  void toDomain_maps_all_fields() {
    var id = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var courseId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    var enrolledAt = Instant.parse("2025-09-01T00:00:00Z");
    var entity =
        StudentCourseEnrollmentEntity.builder()
            .id(id)
            .student(UserEntity.builder().id(studentId).build())
            .course(CourseEntity.builder().id(courseId).build())
            .academicYear(AcademicYearEntity.builder().id(academicYearId).build())
            .enrolledAt(enrolledAt)
            .build();

    var enrollment = mapper.toDomain(entity);

    assertEquals(id, enrollment.getId());
    assertEquals(studentId, enrollment.getStudentId());
    assertEquals(courseId, enrollment.getCourseId());
    assertEquals(academicYearId, enrollment.getAcademicYearId());
    assertEquals(enrolledAt, enrollment.getEnrolledAt());
  }

  @Test
  void toEntity_maps_all_fields() {
    var id = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var courseId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    var enrolledAt = Instant.parse("2025-09-01T00:00:00Z");
    var enrollment =
        StudentCourseEnrollment.builder()
            .id(id)
            .studentId(studentId)
            .courseId(courseId)
            .academicYearId(academicYearId)
            .enrolledAt(enrolledAt)
            .build();

    var entity = mapper.toEntity(enrollment);

    assertEquals(id, entity.getId());
    assertEquals(studentId, entity.getStudent().getId());
    assertEquals(courseId, entity.getCourse().getId());
    assertEquals(academicYearId, entity.getAcademicYear().getId());
    assertEquals(enrolledAt, entity.getEnrolledAt());
  }
}
