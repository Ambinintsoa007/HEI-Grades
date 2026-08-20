package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.GraduateResponse;
import com.example.demo.endpoint.rest.dto.PromotionResultsResponse;
import com.example.demo.service.GraduateExcelService;
import com.example.demo.service.PromotionResultService;
import java.nio.charset.StandardCharsets;
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
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(filename))
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(bytes);
  }

  private String contentDisposition(String filename) {
    return "attachment; filename=\""
        + asciiFallback(filename)
        + "\"; filename*=UTF-8''"
        + rfc5987Encode(filename);
  }

  private String asciiFallback(String filename) {
    return filename.replaceAll("[^\\x20-\\x7E]", "_");
  }

  private String rfc5987Encode(String filename) {
    StringBuilder encoded = new StringBuilder();
    for (byte b : filename.getBytes(StandardCharsets.UTF_8)) {
      int c = b & 0xFF;
      if ((c >= 'a' && c <= 'z')
          || (c >= 'A' && c <= 'Z')
          || (c >= '0' && c <= '9')
          || "!#$&+-.^_`|~".indexOf(c) >= 0) {
        encoded.append((char) c);
      } else {
        encoded.append('%').append(String.format("%02X", c));
      }
    }
    return encoded.toString();
  }
}
