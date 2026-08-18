package com.example.demo.mapper;

import com.example.demo.model.StudentCourseEnrollment;
import com.example.demo.repository.model.*;
import org.springframework.stereotype.Component;

@Component
public class StudentCourseEnrollmentMapper {

  public StudentCourseEnrollment toDomain(StudentCourseEnrollmentEntity entity) {
    return StudentCourseEnrollment.builder()
        .id(entity.getId())
        .studentId(entity.getStudent().getId())
        .courseId(entity.getCourse().getId())
        .academicYearId(entity.getAcademicYear().getId())
        .enrolledAt(entity.getEnrolledAt())
        .courseOfferingIds(
            entity.getCourseOfferings().stream()
                .map(CourseOfferingEntity::getId)
                .collect(java.util.stream.Collectors.toSet()))
        .build();
  }

  public StudentCourseEnrollmentEntity toEntity(StudentCourseEnrollment enrollment) {
    return StudentCourseEnrollmentEntity.builder()
        .id(enrollment.getId())
        .student(UserEntity.builder().id(enrollment.getStudentId()).build())
        .course(CourseEntity.builder().id(enrollment.getCourseId()).build())
        .academicYear(AcademicYearEntity.builder().id(enrollment.getAcademicYearId()).build())
        .enrolledAt(enrollment.getEnrolledAt())
        .courseOfferings(
            enrollment.getCourseOfferingIds().stream()
                .map(id -> CourseOfferingEntity.builder().id(id).build())
                .collect(java.util.stream.Collectors.toSet()))
        .build();
  }
}
