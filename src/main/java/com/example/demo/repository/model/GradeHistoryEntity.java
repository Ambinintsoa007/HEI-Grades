package com.example.demo.repository.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "grade_histories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeHistoryEntity {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "grade_id", nullable = false)
  private GradeEntity grade;

  @Column(name = "old_score", nullable = false)
  private BigDecimal oldScore;

  @Column(name = "new_score", nullable = false)
  private BigDecimal newScore;

  @Column(nullable = false)
  private String reason;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "changed_by", nullable = false)
  private UserEntity changedBy;

  @Column(name = "changed_at", nullable = false)
  private Instant changedAt;
}
