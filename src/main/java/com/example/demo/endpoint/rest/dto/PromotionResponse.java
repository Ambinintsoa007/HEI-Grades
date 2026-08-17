package com.example.demo.endpoint.rest.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PromotionResponse {

  private final UUID id;
  private final String name;
  private final Integer startYear;
  private final Integer endYear;
}
