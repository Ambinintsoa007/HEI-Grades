package com.example.demo.mapper;

import com.example.demo.model.CourseOffering;
import com.example.demo.repository.model.AcademicYearEntity;
import com.example.demo.repository.model.CourseEntity;
import com.example.demo.repository.model.CourseOfferingEntity;
import com.example.demo.repository.model.GroupEntity;
import org.springframework.stereotype.Component;

@Component
public class CourseOfferingMapper {

  public CourseOffering toDomain(CourseOfferingEntity entity) {
    return CourseOffering.builder()
        .id(entity.getId())
        .courseId(entity.getCourse().getId())
        .academicYearId(entity.getAcademicYear().getId())
        .groupId(entity.getGroup().getId())
        .build();
  }

  public CourseOfferingEntity toEntity(CourseOffering offering) {
    return CourseOfferingEntity.builder()
        .id(offering.getId())
        .course(CourseEntity.builder().id(offering.getCourseId()).build())
        .academicYear(AcademicYearEntity.builder().id(offering.getAcademicYearId()).build())
        .group(GroupEntity.builder().id(offering.getGroupId()).build())
        .build();
  }
}
