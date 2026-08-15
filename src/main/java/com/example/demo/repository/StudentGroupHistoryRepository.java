package com.example.demo.repository;

import com.example.demo.repository.model.StudentGroupHistoryEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentGroupHistoryRepository
    extends JpaRepository<StudentGroupHistoryEntity, UUID> {

  List<StudentGroupHistoryEntity> findByStudent_IdOrderByStartedAtAsc(UUID studentId);

  Optional<StudentGroupHistoryEntity> findByStudent_IdAndEndedAtIsNull(UUID studentId);
}
