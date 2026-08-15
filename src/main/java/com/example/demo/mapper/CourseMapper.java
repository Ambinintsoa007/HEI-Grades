package com.example.demo.mapper;

import com.example.demo.model.Course;
import com.example.demo.repository.model.CourseEntity;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

  public Course toDomain(CourseEntity entity) {
    return Course.builder()
        .id(entity.getId())
        .ref(entity.getRef())
        .title(entity.getTitle())
        .credits(entity.getCredits())
        .build();
  }

  public CourseEntity toEntity(Course course) {
    return CourseEntity.builder()
        .id(course.getId())
        .ref(course.getRef())
        .title(course.getTitle())
        .credits(course.getCredits())
        .build();
  }
}
