package com.example.demo.mapper;

import com.example.demo.model.GradeHistory;
import com.example.demo.repository.model.GradeEntity;
import com.example.demo.repository.model.GradeHistoryEntity;
import com.example.demo.repository.model.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class GradeHistoryMapper {

  public GradeHistory toDomain(GradeHistoryEntity entity) {
    return GradeHistory.builder()
        .id(entity.getId())
        .gradeId(entity.getGrade().getId())
        .oldScore(entity.getOldScore())
        .newScore(entity.getNewScore())
        .reason(entity.getReason())
        .changedBy(entity.getChangedBy().getId())
        .changedAt(entity.getChangedAt())
        .build();
  }

  public GradeHistoryEntity toEntity(GradeHistory history) {
    return GradeHistoryEntity.builder()
        .id(history.getId())
        .grade(GradeEntity.builder().id(history.getGradeId()).build())
        .oldScore(history.getOldScore())
        .newScore(history.getNewScore())
        .reason(history.getReason())
        .changedBy(UserEntity.builder().id(history.getChangedBy()).build())
        .changedAt(history.getChangedAt())
        .build();
  }
}
