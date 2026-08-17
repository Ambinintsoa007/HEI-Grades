package com.example.demo.endpoint.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePromotionRequest {

  @NotBlank private String name;
  @NotNull private Integer startYear;
  @NotNull private Integer endYear;
}
