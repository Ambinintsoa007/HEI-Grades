package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.endpoint.rest.dto.CreatePromotionRequest;
import com.example.demo.endpoint.rest.dto.UpdatePromotionRequest;
import com.example.demo.endpoint.rest.exception.BusinessException;
import com.example.demo.endpoint.rest.exception.ConflictException;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.mapper.PromotionMapper;
import com.example.demo.model.Promotion;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.model.PromotionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PromotionServiceTest {

  private PromotionRepository promotionRepository;
  private PromotionMapper promotionMapper;
  private PromotionService promotionService;

  @BeforeEach
  void setUp() {
    promotionRepository = mock(PromotionRepository.class);
    promotionMapper = mock(PromotionMapper.class);

    promotionService = new PromotionService(promotionRepository, promotionMapper);
  }

  @Test
  void shouldGetAllPromotions() {
    PromotionEntity entity = new PromotionEntity();

    Promotion promotion =
        Promotion.builder()
            .id(UUID.randomUUID())
            .name("2025")
            .startYear(2025)
            .endYear(2028)
            .build();

    when(promotionRepository.findAll()).thenReturn(List.of(entity));
    when(promotionMapper.toDomain(entity)).thenReturn(promotion);

    var result = promotionService.getAll();

    assertEquals(1, result.size());
    assertEquals("2025", result.getFirst().getName());
  }

  @Test
  void shouldCreatePromotion() {
    CreatePromotionRequest request = new CreatePromotionRequest();
    request.setName("2025");
    request.setStartYear(2025);
    request.setEndYear(2028);

    PromotionEntity entity = new PromotionEntity();

    Promotion saved =
        Promotion.builder()
            .id(UUID.randomUUID())
            .name("2025")
            .startYear(2025)
            .endYear(2028)
            .build();

    when(promotionRepository.existsByNameIgnoreCase("2025")).thenReturn(false);
    when(promotionMapper.toEntity(any(Promotion.class))).thenReturn(entity);
    when(promotionRepository.save(entity)).thenReturn(entity);
    when(promotionMapper.toDomain(entity)).thenReturn(saved);

    var result = promotionService.create(request);

    assertEquals("2025", result.getName());
    assertEquals(2025, result.getStartYear());
    assertEquals(2028, result.getEndYear());
  }

  @Test
  void shouldRejectDuplicatePromotion() {
    CreatePromotionRequest request = new CreatePromotionRequest();
    request.setName("2025");
    request.setStartYear(2025);
    request.setEndYear(2028);

    when(promotionRepository.existsByNameIgnoreCase("2025")).thenReturn(true);

    assertThrows(ConflictException.class, () -> promotionService.create(request));
  }

  @Test
  void shouldRejectInvalidYears() {
    CreatePromotionRequest request = new CreatePromotionRequest();
    request.setName("2025");
    request.setStartYear(2028);
    request.setEndYear(2025);

    assertThrows(BusinessException.class, () -> promotionService.create(request));
  }

  @Test
  void shouldRejectUnknownPromotionOnUpdate() {
    UUID id = UUID.randomUUID();

    when(promotionRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> promotionService.update(id, new UpdatePromotionRequest()));
  }

  @Test
  void shouldUpdatePromotion() {
    UUID id = UUID.randomUUID();

    PromotionEntity entity = new PromotionEntity();

    Promotion current =
        Promotion.builder().id(id).name("2025").startYear(2025).endYear(2028).build();

    Promotion updated =
        Promotion.builder().id(id).name("Promotion 2025").startYear(2025).endYear(2028).build();

    UpdatePromotionRequest request = new UpdatePromotionRequest();
    request.setName("Promotion 2025");

    when(promotionRepository.findById(id)).thenReturn(Optional.of(entity));
    when(promotionMapper.toDomain(entity)).thenReturn(current, updated);
    when(promotionMapper.toEntity(any(Promotion.class))).thenReturn(entity);
    when(promotionRepository.save(entity)).thenReturn(entity);

    var result = promotionService.update(id, request);

    assertEquals("Promotion 2025", result.getName());
  }
}
