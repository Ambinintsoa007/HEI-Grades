package com.example.demo.mapper;

import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;
import com.example.demo.repository.model.PromotionEntity;
import com.example.demo.repository.model.UserEntity;
import com.example.demo.repository.model.UserRoleEntity;
import com.example.demo.repository.model.UserStatusEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  public User toDomain(UserEntity entity) {
    return User.builder()
        .id(entity.getId())
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .email(entity.getEmail())
        .passwordHash(entity.getPasswordHash())
        .role(UserRole.valueOf(entity.getRole().name()))
        .status(UserStatus.valueOf(entity.getStatus().name()))
        .std(entity.getStd())
        .promotionId(entity.getPromotion() == null ? null : entity.getPromotion().getId())
        .build();
  }

  public UserEntity toEntity(User user) {
    return UserEntity.builder()
        .id(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .passwordHash(user.getPasswordHash())
        .role(UserRoleEntity.valueOf(user.getRole().name()))
        .status(UserStatusEntity.valueOf(user.getStatus().name()))
        .std(user.getStd())
        .promotion(
            user.getPromotionId() == null
                ? null
                : PromotionEntity.builder().id(user.getPromotionId()).build())
        .build();
  }
}
