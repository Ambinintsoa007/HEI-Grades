package com.example.demo.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.demo.model.Pathway;
import com.example.demo.model.StudentGroupHistory;
import com.example.demo.repository.model.AcademicYearEntity;
import com.example.demo.repository.model.GroupEntity;
import com.example.demo.repository.model.PathwayEntity;
import com.example.demo.repository.model.StudentGroupHistoryEntity;
import com.example.demo.repository.model.UserEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StudentGroupHistoryMapperTest {

  private final StudentGroupHistoryMapper mapper = new StudentGroupHistoryMapper();

  @Test
  void toDomain_maps_all_fields_with_pathway() {
    var id = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    var groupId = UUID.randomUUID();
    var startedAt = Instant.parse("2025-01-01T00:00:00Z");
    var endedAt = Instant.parse("2025-06-01T00:00:00Z");
    var entity =
        StudentGroupHistoryEntity.builder()
            .id(id)
            .student(UserEntity.builder().id(studentId).build())
            .academicYear(AcademicYearEntity.builder().id(academicYearId).build())
            .group(GroupEntity.builder().id(groupId).build())
            .pathway(PathwayEntity.TN)
            .startedAt(startedAt)
            .endedAt(endedAt)
            .build();

    var history = mapper.toDomain(entity);

    assertEquals(id, history.getId());
    assertEquals(studentId, history.getStudentId());
    assertEquals(academicYearId, history.getAcademicYearId());
    assertEquals(groupId, history.getGroupId());
    assertEquals(Pathway.TN, history.getPathway());
    assertEquals(startedAt, history.getStartedAt());
    assertEquals(endedAt, history.getEndedAt());
  }

  @Test
  void toDomain_maps_null_pathway() {
    var entity =
        StudentGroupHistoryEntity.builder()
            .id(UUID.randomUUID())
            .student(UserEntity.builder().id(UUID.randomUUID()).build())
            .academicYear(AcademicYearEntity.builder().id(UUID.randomUUID()).build())
            .group(GroupEntity.builder().id(UUID.randomUUID()).build())
            .startedAt(Instant.now())
            .build();

    var history = mapper.toDomain(entity);

    assertNull(history.getPathway());
  }

  @Test
  void toEntity_maps_all_fields_with_pathway() {
    var id = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    var groupId = UUID.randomUUID();
    var startedAt = Instant.parse("2025-01-01T00:00:00Z");
    var history =
        StudentGroupHistory.builder()
            .id(id)
            .studentId(studentId)
            .academicYearId(academicYearId)
            .groupId(groupId)
            .pathway(Pathway.EL)
            .startedAt(startedAt)
            .build();

    var entity = mapper.toEntity(history);

    assertEquals(id, entity.getId());
    assertEquals(studentId, entity.getStudent().getId());
    assertEquals(academicYearId, entity.getAcademicYear().getId());
    assertEquals(groupId, entity.getGroup().getId());
    assertEquals(PathwayEntity.EL, entity.getPathway());
    assertEquals(startedAt, entity.getStartedAt());
  }

  @Test
  void toEntity_maps_null_pathway() {
    var history =
        StudentGroupHistory.builder()
            .id(UUID.randomUUID())
            .studentId(UUID.randomUUID())
            .academicYearId(UUID.randomUUID())
            .groupId(UUID.randomUUID())
            .startedAt(Instant.now())
            .build();

    var entity = mapper.toEntity(history);

    assertNull(entity.getPathway());
  }
}
