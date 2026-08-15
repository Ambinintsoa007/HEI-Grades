package com.example.demo.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Group {

  private final UUID id;
  private final String ref;
}
