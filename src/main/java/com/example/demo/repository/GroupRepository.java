package com.example.demo.repository;

import com.example.demo.repository.model.GroupEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, UUID> {

  Optional<GroupEntity> findByRefIgnoreCase(String ref);

  boolean existsByRefIgnoreCase(String ref);

  boolean existsByRefIgnoreCaseAndIdNot(String ref, UUID id);
}
