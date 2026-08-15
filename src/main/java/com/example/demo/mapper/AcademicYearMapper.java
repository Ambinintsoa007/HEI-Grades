package com.example.demo.mapper;

import com.example.demo.model.AcademicYear;
import com.example.demo.repository.model.AcademicYearEntity;
import org.springframework.stereotype.Component;

@Component
public class AcademicYearMapper {

  public AcademicYear toDomain(AcademicYearEntity entity) {
    return AcademicYear.builder()
        .id(entity.getId())
        .label(entity.getLabel())
        .startDate(entity.getStartDate())
        .endDate(entity.getEndDate())
        .build();
  }

  public AcademicYearEntity toEntity(AcademicYear academicYear) {
    return AcademicYearEntity.builder()
        .id(academicYear.getId())
        .label(academicYear.getLabel())
        .startDate(academicYear.getStartDate())
        .endDate(academicYear.getEndDate())
        .build();
  }
}
