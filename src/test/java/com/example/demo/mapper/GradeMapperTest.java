package com.example.demo.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.model.Grade;
import com.example.demo.repository.model.ExamEntity;
import com.example.demo.repository.model.GradeEntity;
import com.example.demo.repository.model.StudentCourseEnrollmentEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GradeMapperTest {

  private final GradeMapper mapper = new GradeMapper();

  @Test
  void toDomain_maps_all_fields() {
    var id = UUID.randomUUID();
    var examId = UUID.randomUUID();
    var enrollmentId = UUID.randomUUID();
    var updatedAt = Instant.parse("2025-04-01T00:00:00Z");
    var entity =
        GradeEntity.builder()
            .id(id)
            .exam(ExamEntity.builder().id(examId).build())
            .studentCourseEnrollment(
                StudentCourseEnrollmentEntity.builder().id(enrollmentId).build())
            .score(new BigDecimal("15"))
            .updatedAt(updatedAt)
            .build();

    var grade = mapper.toDomain(entity);

    assertEquals(id, grade.getId());
    assertEquals(examId, grade.getExamId());
    assertEquals(enrollmentId, grade.getStudentCourseEnrollmentId());
    assertEquals(new BigDecimal("15"), grade.getScore());
    assertEquals(updatedAt, grade.getUpdatedAt());
  }

  @Test
  void toEntity_maps_all_fields() {
    var id = UUID.randomUUID();
    var examId = UUID.randomUUID();
    var enrollmentId = UUID.randomUUID();
    var updatedAt = Instant.parse("2025-04-01T00:00:00Z");
    var grade =
        Grade.builder()
            .id(id)
            .examId(examId)
            .studentCourseEnrollmentId(enrollmentId)
            .score(new BigDecimal("15"))
            .updatedAt(updatedAt)
            .build();

    var entity = mapper.toEntity(grade);

    assertEquals(id, entity.getId());
    assertEquals(examId, entity.getExam().getId());
    assertEquals(enrollmentId, entity.getStudentCourseEnrollment().getId());
    assertEquals(new BigDecimal("15"), entity.getScore());
    assertEquals(updatedAt, entity.getUpdatedAt());
  }
}
