package com.example.demo.repository.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "grades")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeEntity {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "exam_id", nullable = false)
  private ExamEntity exam;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "student_course_enrollment_id", nullable = false)
  private StudentCourseEnrollmentEntity studentCourseEnrollment;

  private BigDecimal score;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
