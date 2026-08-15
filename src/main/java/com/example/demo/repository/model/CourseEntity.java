package com.example.demo.repository.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "courses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private String ref;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private Integer credits;
}
