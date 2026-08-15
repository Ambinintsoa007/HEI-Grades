package com.example.demo.repository;

import com.example.demo.repository.model.ExamEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamRepository extends JpaRepository<ExamEntity, UUID> {

  List<ExamEntity> findByCourseOffering_Id(UUID courseOfferingId);

  Optional<ExamEntity> findByCourseOffering_IdAndRefIgnoreCase(UUID courseOfferingId, String ref);
}
