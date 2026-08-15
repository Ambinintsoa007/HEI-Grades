package com.example.demo.mapper;

import com.example.demo.model.Group;
import com.example.demo.repository.model.GroupEntity;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

  public Group toDomain(GroupEntity entity) {
    return Group.builder().id(entity.getId()).ref(entity.getRef()).build();
  }

  public GroupEntity toEntity(Group group) {
    return GroupEntity.builder().id(group.getId()).ref(group.getRef()).build();
  }
}
