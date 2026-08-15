package com.example.demo.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.model.Group;
import com.example.demo.repository.model.GroupEntity;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GroupMapperTest {

  private final GroupMapper mapper = new GroupMapper();

  @Test
  void toDomain_maps_all_fields() {
    var id = UUID.randomUUID();
    var entity = GroupEntity.builder().id(id).ref("G1").build();

    var group = mapper.toDomain(entity);

    assertEquals(id, group.getId());
    assertEquals("G1", group.getRef());
  }

  @Test
  void toEntity_maps_all_fields() {
    var id = UUID.randomUUID();
    var group = Group.builder().id(id).ref("G1").build();

    var entity = mapper.toEntity(group);

    assertEquals(id, entity.getId());
    assertEquals("G1", entity.getRef());
  }
}
