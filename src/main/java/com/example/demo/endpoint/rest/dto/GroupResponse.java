package com.example.demo.endpoint.rest.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupResponse {

  private final UUID id;
  private final String ref;
}
