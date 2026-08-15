package com.example.demo.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.model.GradeHistory;
import com.example.demo.repository.model.GradeEntity;
import com.example.demo.repository.model.GradeHistoryEntity;
import com.example.demo.repository.model.UserEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GradeHistoryMapperTest {

  private final GradeHistoryMapper mapper = new GradeHistoryMapper();

  @Test
  void toDomain_maps_all_fields() {
    var id = UUID.randomUUID();
    var gradeId = UUID.randomUUID();
    var changedBy = UUID.randomUUID();
    var changedAt = Instant.parse("2025-03-01T00:00:00Z");
    var entity =
        GradeHistoryEntity.builder()
            .id(id)
            .grade(GradeEntity.builder().id(gradeId).build())
            .oldScore(new BigDecimal("8"))
            .newScore(new BigDecimal("12"))
            .reason("Rattrapage")
            .changedBy(UserEntity.builder().id(changedBy).build())
            .changedAt(changedAt)
            .build();

    var history = mapper.toDomain(entity);

    assertEquals(id, history.getId());
    assertEquals(gradeId, history.getGradeId());
    assertEquals(new BigDecimal("8"), history.getOldScore());
    assertEquals(new BigDecimal("12"), history.getNewScore());
    assertEquals("Rattrapage", history.getReason());
    assertEquals(changedBy, history.getChangedBy());
    assertEquals(changedAt, history.getChangedAt());
  }

  @Test
  void toEntity_maps_all_fields() {
    var id = UUID.randomUUID();
    var gradeId = UUID.randomUUID();
    var changedBy = UUID.randomUUID();
    var changedAt = Instant.parse("2025-03-01T00:00:00Z");
    var history =
        GradeHistory.builder()
            .id(id)
            .gradeId(gradeId)
            .oldScore(new BigDecimal("8"))
            .newScore(new BigDecimal("12"))
            .reason("Rattrapage")
            .changedBy(changedBy)
            .changedAt(changedAt)
            .build();

    var entity = mapper.toEntity(history);

    assertEquals(id, entity.getId());
    assertEquals(gradeId, entity.getGrade().getId());
    assertEquals(new BigDecimal("8"), entity.getOldScore());
    assertEquals(new BigDecimal("12"), entity.getNewScore());
    assertEquals("Rattrapage", entity.getReason());
    assertEquals(changedBy, entity.getChangedBy().getId());
    assertEquals(changedAt, entity.getChangedAt());
  }
}
