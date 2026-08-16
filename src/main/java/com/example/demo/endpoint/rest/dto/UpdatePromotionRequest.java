package com.example.demo.endpoint.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePromotionRequest {

  private String name;
  private Integer startYear;
  private Integer endYear;
}
