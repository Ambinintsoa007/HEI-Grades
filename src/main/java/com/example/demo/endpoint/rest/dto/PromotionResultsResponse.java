package com.example.demo.endpoint.rest.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PromotionResultsResponse {

  private final UUID promotionId;
  private final List<PromotionStudentResultResponse> students;
}
