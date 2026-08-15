package com.example.demo.mapper;

import com.example.demo.model.Exam;
import com.example.demo.repository.model.CourseOfferingEntity;
import com.example.demo.repository.model.ExamEntity;
import org.springframework.stereotype.Component;

@Component
public class ExamMapper {

  public Exam toDomain(ExamEntity entity) {
    return Exam.builder()
        .id(entity.getId())
        .ref(entity.getRef())
        .courseOfferingId(entity.getCourseOffering().getId())
        .coefficient(entity.getCoefficient())
        .build();
  }

  public ExamEntity toEntity(Exam exam) {
    return ExamEntity.builder()
        .id(exam.getId())
        .ref(exam.getRef())
        .courseOffering(CourseOfferingEntity.builder().id(exam.getCourseOfferingId()).build())
        .coefficient(exam.getCoefficient())
        .build();
  }
}
