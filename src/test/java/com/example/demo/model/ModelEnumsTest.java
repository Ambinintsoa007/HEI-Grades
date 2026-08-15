package com.example.demo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.repository.model.PathwayEntity;
import com.example.demo.repository.model.UserRoleEntity;
import com.example.demo.repository.model.UserStatusEntity;
import org.junit.jupiter.api.Test;

class ModelEnumsTest {

  @Test
  void domain_enums_cover_all_values() {
    assertEquals(3, UserRole.values().length);
    assertEquals(2, UserStatus.values().length);
    assertEquals(2, Pathway.values().length);
  }

  @Test
  void entity_enums_cover_all_values() {
    assertEquals(3, UserRoleEntity.values().length);
    assertEquals(2, UserStatusEntity.values().length);
    assertEquals(2, PathwayEntity.values().length);
  }
}
