package com.example.demo.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Course {
  private final UUID id;
  private final String ref;
  private final String title;
  private final int credits;
}
