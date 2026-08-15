package com.example.demo.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.model.AcademicYear;
import com.example.demo.repository.model.AcademicYearEntity;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AcademicYearMapperTest {

  private final AcademicYearMapper mapper = new AcademicYearMapper();

  @Test
  void toDomain_maps_all_fields() {
    var id = UUID.randomUUID();
    var start = LocalDate.of(2025, 9, 1);
    var end = LocalDate.of(2026, 8, 31);
    var entity =
        AcademicYearEntity.builder()
            .id(id)
            .label("2025-2026")
            .startDate(start)
            .endDate(end)
            .build();

    var year = mapper.toDomain(entity);

    assertEquals(id, year.getId());
    assertEquals("2025-2026", year.getLabel());
    assertEquals(start, year.getStartDate());
    assertEquals(end, year.getEndDate());
  }

  @Test
  void toEntity_maps_all_fields() {
    var id = UUID.randomUUID();
    var start = LocalDate.of(2025, 9, 1);
    var end = LocalDate.of(2026, 8, 31);
    var year =
        AcademicYear.builder().id(id).label("2025-2026").startDate(start).endDate(end).build();

    var entity = mapper.toEntity(year);

    assertEquals(id, entity.getId());
    assertEquals("2025-2026", entity.getLabel());
    assertEquals(start, entity.getStartDate());
    assertEquals(end, entity.getEndDate());
  }
}
