package com.example.demo.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(name = "start_year", nullable = false)
  private Integer startYear;

  @Column(name = "end_year", nullable = false)
  private Integer endYear;
}
