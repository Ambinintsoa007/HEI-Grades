package com.example.demo.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;
import com.example.demo.repository.model.PromotionEntity;
import com.example.demo.repository.model.UserEntity;
import com.example.demo.repository.model.UserRoleEntity;
import com.example.demo.repository.model.UserStatusEntity;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserMapperTest {

  private final UserMapper mapper = new UserMapper();

  @Test
  void toDomain_maps_all_fields_with_promotion() {
    var id = UUID.randomUUID();
    var promotionId = UUID.randomUUID();
    var entity =
        UserEntity.builder()
            .id(id)
            .firstName("John")
            .lastName("Doe")
            .email("john@hei.school")
            .passwordHash("hash")
            .role(UserRoleEntity.TEACHER)
            .status(UserStatusEntity.ACTIVE)
            .std("STD-1")
            .promotion(PromotionEntity.builder().id(promotionId).build())
            .build();

    var user = mapper.toDomain(entity);

    assertEquals(id, user.getId());
    assertEquals("John", user.getFirstName());
    assertEquals("Doe", user.getLastName());
    assertEquals("john@hei.school", user.getEmail());
    assertEquals("hash", user.getPasswordHash());
    assertEquals(UserRole.TEACHER, user.getRole());
    assertEquals(UserStatus.ACTIVE, user.getStatus());
    assertEquals("STD-1", user.getStd());
    assertEquals(promotionId, user.getPromotionId());
  }

  @Test
  void toDomain_maps_null_promotion() {
    var entity =
        UserEntity.builder()
            .id(UUID.randomUUID())
            .firstName("A")
            .lastName("B")
            .email("a@hei.school")
            .passwordHash("h")
            .role(UserRoleEntity.STUDENT)
            .status(UserStatusEntity.DISABLED)
            .build();

    var user = mapper.toDomain(entity);

    assertNull(user.getPromotionId());
  }

  @Test
  void toEntity_maps_all_fields_with_promotion() {
    var id = UUID.randomUUID();
    var promotionId = UUID.randomUUID();
    var user =
        User.builder()
            .id(id)
            .firstName("Jane")
            .lastName("Smith")
            .email("jane@hei.school")
            .passwordHash("hash2")
            .role(UserRole.ADMIN)
            .status(UserStatus.ACTIVE)
            .promotionId(promotionId)
            .build();

    var entity = mapper.toEntity(user);

    assertEquals(id, entity.getId());
    assertEquals(UserRoleEntity.ADMIN, entity.getRole());
    assertEquals(UserStatusEntity.ACTIVE, entity.getStatus());
    assertEquals(promotionId, entity.getPromotion().getId());
  }

  @Test
  void toEntity_maps_null_promotion() {
    var user =
        User.builder()
            .id(UUID.randomUUID())
            .firstName("A")
            .lastName("B")
            .email("a@hei.school")
            .passwordHash("h")
            .role(UserRole.STUDENT)
            .status(UserStatus.DISABLED)
            .build();

    var entity = mapper.toEntity(user);

    assertNull(entity.getPromotion());
  }
}
