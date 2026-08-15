package com.example.demo.mapper;

import com.example.demo.model.Promotion;
import com.example.demo.repository.model.PromotionEntity;
import org.springframework.stereotype.Component;

@Component
public class PromotionMapper {

  public Promotion toDomain(PromotionEntity entity) {
    return Promotion.builder()
        .id(entity.getId())
        .name(entity.getName())
        .startYear(entity.getStartYear())
        .endYear(entity.getEndYear())
        .build();
  }

  public PromotionEntity toEntity(Promotion promotion) {
    return PromotionEntity.builder()
        .id(promotion.getId())
        .name(promotion.getName())
        .startYear(promotion.getStartYear())
        .endYear(promotion.getEndYear())
        .build();
  }
}
