package com.example.demo.repository;

import com.example.demo.repository.model.GradeHistoryEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeHistoryRepository extends JpaRepository<GradeHistoryEntity, UUID> {

  List<GradeHistoryEntity> findByGrade_IdOrderByChangedAtAsc(UUID gradeId);
}
