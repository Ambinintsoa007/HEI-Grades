package com.example.demo.repository.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "exams")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private String ref;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_offering_id", nullable = false)
  private CourseOfferingEntity courseOffering;

  @Column(nullable = false)
  private BigDecimal coefficient;
}
