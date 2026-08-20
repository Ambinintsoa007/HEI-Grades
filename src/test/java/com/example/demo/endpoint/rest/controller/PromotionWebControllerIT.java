package com.example.demo.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.CourseManagementTestBase;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.model.PromotionEntity;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

class PromotionWebControllerIT extends CourseManagementTestBase {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private PromotionRepository promotionRepository;

  @Test
  void promotionsPageRendersPromotions() {
    promotionRepository.save(
        PromotionEntity.builder()
            .id(UUID.randomUUID())
            .name("Promo 2027 " + UUID.randomUUID().toString().substring(0, 8))
            .startYear(2024)
            .endYear(2027)
            .build());
    promotionRepository.save(
        PromotionEntity.builder()
            .id(UUID.randomUUID())
            .name("Promo 2026 " + UUID.randomUUID().toString().substring(0, 8))
            .startYear(2023)
            .endYear(2026)
            .build());

    var response = restTemplate.getForEntity("/web/promotions", String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("Promo 2027 "));
    assertTrue(response.getBody().contains("Promo 2026 "));
    assertTrue(response.getBody().contains("2024 - 2027"));
    assertTrue(response.getBody().contains("Télécharger diplômés"));
    assertTrue(response.getBody().contains("Résultats"));
    assertTrue(response.getBody().contains("role-badge\">ADMIN"));
  }
}
