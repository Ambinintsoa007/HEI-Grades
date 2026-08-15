package com.example.demo.repository;

import com.example.demo.repository.model.PromotionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, UUID> {

  Optional<PromotionEntity> findByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCase(String name);
}
