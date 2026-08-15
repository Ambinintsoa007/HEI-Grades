package com.example.demo.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.model.Promotion;
import com.example.demo.repository.model.PromotionEntity;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PromotionMapperTest {

  private final PromotionMapper mapper = new PromotionMapper();

  @Test
  void toDomain_maps_all_fields() {
    var id = UUID.randomUUID();
    var entity =
        PromotionEntity.builder().id(id).name("HEI 2025").startYear(2025).endYear(2026).build();

    var promotion = mapper.toDomain(entity);

    assertEquals(id, promotion.getId());
    assertEquals("HEI 2025", promotion.getName());
    assertEquals(2025, promotion.getStartYear());
    assertEquals(2026, promotion.getEndYear());
  }

  @Test
  void toEntity_maps_all_fields() {
    var id = UUID.randomUUID();
    var promotion =
        Promotion.builder().id(id).name("HEI 2025").startYear(2025).endYear(2026).build();

    var entity = mapper.toEntity(promotion);

    assertEquals(id, entity.getId());
    assertEquals("HEI 2025", entity.getName());
    assertEquals(2025, entity.getStartYear());
    assertEquals(2026, entity.getEndYear());
  }
}
