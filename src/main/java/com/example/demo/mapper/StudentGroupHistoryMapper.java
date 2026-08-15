package com.example.demo.mapper;

import com.example.demo.model.Pathway;
import com.example.demo.model.StudentGroupHistory;
import com.example.demo.repository.model.AcademicYearEntity;
import com.example.demo.repository.model.GroupEntity;
import com.example.demo.repository.model.PathwayEntity;
import com.example.demo.repository.model.StudentGroupHistoryEntity;
import com.example.demo.repository.model.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class StudentGroupHistoryMapper {

  public StudentGroupHistory toDomain(StudentGroupHistoryEntity entity) {
    return StudentGroupHistory.builder()
        .id(entity.getId())
        .studentId(entity.getStudent().getId())
        .academicYearId(entity.getAcademicYear().getId())
        .groupId(entity.getGroup().getId())
        .pathway(entity.getPathway() == null ? null : Pathway.valueOf(entity.getPathway().name()))
        .startedAt(entity.getStartedAt())
        .endedAt(entity.getEndedAt())
        .build();
  }

  public StudentGroupHistoryEntity toEntity(StudentGroupHistory history) {
    return StudentGroupHistoryEntity.builder()
        .id(history.getId())
        .student(UserEntity.builder().id(history.getStudentId()).build())
        .academicYear(AcademicYearEntity.builder().id(history.getAcademicYearId()).build())
        .group(GroupEntity.builder().id(history.getGroupId()).build())
        .pathway(
            history.getPathway() == null
                ? null
                : PathwayEntity.valueOf(history.getPathway().name()))
        .startedAt(history.getStartedAt())
        .endedAt(history.getEndedAt())
        .build();
  }
}
