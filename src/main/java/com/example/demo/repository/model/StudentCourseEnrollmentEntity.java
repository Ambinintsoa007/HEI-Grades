package com.example.demo.repository.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "student_course_enrollments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseEnrollmentEntity {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private UserEntity student;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id", nullable = false)
  private CourseEntity course;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "academic_year_id", nullable = false)
  private AcademicYearEntity academicYear;

  @Column(name = "enrolled_at", nullable = false)
  private Instant enrolledAt;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "student_course_enrollment_offerings",
      joinColumns = @JoinColumn(name = "student_course_enrollment_id"),
      inverseJoinColumns = @JoinColumn(name = "course_offering_id"))
  @Builder.Default
  private Set<CourseOfferingEntity> courseOfferings = new HashSet<>();
}
