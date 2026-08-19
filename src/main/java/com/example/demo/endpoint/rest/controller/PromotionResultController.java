package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.GraduateResponse;
import com.example.demo.endpoint.rest.dto.PromotionResultsResponse;
import com.example.demo.service.GraduateExcelService;
import com.example.demo.service.PromotionResultService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionResultController {

  private final PromotionResultService promotionResultService;
  private final GraduateExcelService graduateExcelService;

  @GetMapping("/{promotionId}/results")
  public PromotionResultsResponse getPromotionResults(@PathVariable UUID promotionId) {
    return promotionResultService.getPromotionResults(promotionId);
  }

  @GetMapping("/{promotionId}/graduates")
  public List<GraduateResponse> getGraduates(@PathVariable UUID promotionId) {
    return promotionResultService.getGraduates(promotionId);
  }

  @GetMapping("/{promotionId}/graduates.xlsx")
  public ResponseEntity<byte[]> downloadGraduates(@PathVariable UUID promotionId) {
    byte[] bytes = graduateExcelService.generate(promotionId);
    String filename = graduateExcelService.filename(promotionId);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(bytes);
  }
}
