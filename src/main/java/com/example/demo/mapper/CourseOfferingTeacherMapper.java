package com.example.demo.mapper;

import com.example.demo.model.CourseOfferingTeacher;
import com.example.demo.repository.model.CourseOfferingEntity;
import com.example.demo.repository.model.CourseOfferingTeacherEntity;
import com.example.demo.repository.model.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class CourseOfferingTeacherMapper {

  public CourseOfferingTeacher toDomain(CourseOfferingTeacherEntity entity) {
    return CourseOfferingTeacher.builder()
        .id(entity.getId())
        .courseOfferingId(entity.getCourseOffering().getId())
        .teacherId(entity.getTeacher().getId())
        .build();
  }

  public CourseOfferingTeacherEntity toEntity(CourseOfferingTeacher assignment) {
    return CourseOfferingTeacherEntity.builder()
        .id(assignment.getId())
        .courseOffering(CourseOfferingEntity.builder().id(assignment.getCourseOfferingId()).build())
        .teacher(UserEntity.builder().id(assignment.getTeacherId()).build())
        .build();
  }
}
