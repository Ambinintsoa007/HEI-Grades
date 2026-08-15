package com.example.demo.mapper;

import com.example.demo.model.Grade;
import com.example.demo.repository.model.ExamEntity;
import com.example.demo.repository.model.GradeEntity;
import com.example.demo.repository.model.StudentCourseEnrollmentEntity;
import org.springframework.stereotype.Component;

@Component
public class GradeMapper {

  public Grade toDomain(GradeEntity entity) {
    return Grade.builder()
        .id(entity.getId())
        .examId(entity.getExam().getId())
        .studentCourseEnrollmentId(entity.getStudentCourseEnrollment().getId())
        .score(entity.getScore())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  public GradeEntity toEntity(Grade grade) {
    return GradeEntity.builder()
        .id(grade.getId())
        .exam(ExamEntity.builder().id(grade.getExamId()).build())
        .studentCourseEnrollment(
            StudentCourseEnrollmentEntity.builder()
                .id(grade.getStudentCourseEnrollmentId())
                .build())
        .score(grade.getScore())
        .updatedAt(grade.getUpdatedAt())
        .build();
  }
}
