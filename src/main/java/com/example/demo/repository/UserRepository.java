package com.example.demo.repository;

import com.example.demo.repository.model.UserEntity;
import com.example.demo.repository.model.UserRoleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

  Optional<UserEntity> findByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCase(String email);

  Optional<UserEntity> findByStdIgnoreCase(String std);

  boolean existsByStdIgnoreCase(String std);

  boolean existsByRole(UserRoleEntity role);

  List<UserEntity> findByRoleAndPromotion_Id(UserRoleEntity role, UUID promotionId);
}
