package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.CreatePromotionRequest;
import com.example.demo.endpoint.rest.dto.PromotionResponse;
import com.example.demo.endpoint.rest.dto.UpdatePromotionRequest;
import com.example.demo.service.PromotionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionController {

  private final PromotionService promotionService;

  @GetMapping
  public ResponseEntity<List<PromotionResponse>> getPromotions() {
    return ResponseEntity.ok(promotionService.getAll());
  }

  @PostMapping
  public ResponseEntity<PromotionResponse> createPromotion(
      @Valid @RequestBody CreatePromotionRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.create(request));
  }

  @PatchMapping("/{promotionId}")
  public ResponseEntity<PromotionResponse> updatePromotion(
      @PathVariable UUID promotionId, @RequestBody UpdatePromotionRequest request) {
    return ResponseEntity.ok(promotionService.update(promotionId, request));
  }
}
