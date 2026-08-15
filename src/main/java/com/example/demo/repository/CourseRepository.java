package com.example.demo.repository;

import com.example.demo.repository.model.CourseEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {

  Optional<CourseEntity> findByRefIgnoreCase(String ref);

  boolean existsByRefIgnoreCase(String ref);
}
