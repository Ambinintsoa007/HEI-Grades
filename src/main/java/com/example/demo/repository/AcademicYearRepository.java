package com.example.demo.repository;

import com.example.demo.repository.model.AcademicYearEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYearEntity, UUID> {

  Optional<AcademicYearEntity> findByLabelIgnoreCase(String label);

  boolean existsByLabelIgnoreCase(String label);
}
