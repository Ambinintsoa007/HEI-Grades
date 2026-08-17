package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.CreatePromotionRequest;
import com.example.demo.endpoint.rest.dto.PromotionResponse;
import com.example.demo.endpoint.rest.dto.UpdatePromotionRequest;
import com.example.demo.endpoint.rest.exception.BusinessException;
import com.example.demo.endpoint.rest.exception.ConflictException;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.mapper.PromotionMapper;
import com.example.demo.model.Promotion;
import com.example.demo.repository.PromotionRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromotionService {

  private final PromotionRepository promotionRepository;
  private final PromotionMapper promotionMapper;

  public List<PromotionResponse> getAll() {
    return promotionRepository.findAll().stream()
        .map(promotionMapper::toDomain)
        .map(this::toResponse)
        .toList();
  }

  public PromotionResponse create(CreatePromotionRequest request) {
    validateYears(request.getStartYear(), request.getEndYear());

    if (promotionRepository.existsByNameIgnoreCase(request.getName())) {
      throw new ConflictException("Promotion already exists");
    }

    Promotion promotion =
        Promotion.builder()
            .id(UUID.randomUUID())
            .name(request.getName())
            .startYear(request.getStartYear())
            .endYear(request.getEndYear())
            .build();

    return toResponse(
        promotionMapper.toDomain(promotionRepository.save(promotionMapper.toEntity(promotion))));
  }

  public PromotionResponse update(UUID promotionId, UpdatePromotionRequest request) {
    Promotion current =
        promotionRepository
            .findById(promotionId)
            .map(promotionMapper::toDomain)
            .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

    String name = request.getName() != null ? request.getName() : current.getName();
    Integer startYear =
        request.getStartYear() != null ? request.getStartYear() : current.getStartYear();
    Integer endYear = request.getEndYear() != null ? request.getEndYear() : current.getEndYear();

    validateYears(startYear, endYear);

    Promotion updated =
        Promotion.builder()
            .id(current.getId())
            .name(name)
            .startYear(startYear)
            .endYear(endYear)
            .build();

    return toResponse(
        promotionMapper.toDomain(promotionRepository.save(promotionMapper.toEntity(updated))));
  }

  private void validateYears(Integer startYear, Integer endYear) {
    if (endYear < startYear) {
      throw new BusinessException("Promotion end year must be greater than or equal to start year");
    }
  }

  private PromotionResponse toResponse(Promotion promotion) {
    return PromotionResponse.builder()
        .id(promotion.getId())
        .name(promotion.getName())
        .startYear(promotion.getStartYear())
        .endYear(promotion.getEndYear())
        .build();
  }
}
