package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.endpoint.rest.dto.CreateGroupRequest;
import com.example.demo.endpoint.rest.dto.UpdateGroupRequest;
import com.example.demo.endpoint.rest.exception.ConflictException;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.mapper.GroupMapper;
import com.example.demo.model.Group;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.model.GroupEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupServiceTest {

  private GroupRepository groupRepository;
  private GroupMapper groupMapper;
  private GroupService groupService;

  @BeforeEach
  void setUp() {
    groupRepository = mock(GroupRepository.class);
    groupMapper = mock(GroupMapper.class);

    groupService = new GroupService(groupRepository, groupMapper);
  }

  @Test
  void shouldGetAllGroups() {
    GroupEntity entity = new GroupEntity();

    Group group = Group.builder().id(UUID.randomUUID()).ref("K1").build();

    when(groupRepository.findAll()).thenReturn(List.of(entity));
    when(groupMapper.toDomain(entity)).thenReturn(group);

    var result = groupService.getAll();

    assertEquals(1, result.size());
    assertEquals("K1", result.getFirst().getRef());
  }

  @Test
  void shouldCreateGroup() {
    CreateGroupRequest request = new CreateGroupRequest();
    request.setRef("K1");

    GroupEntity entity = new GroupEntity();

    Group saved = Group.builder().id(UUID.randomUUID()).ref("K1").build();

    when(groupRepository.existsByRefIgnoreCase("K1")).thenReturn(false);
    when(groupMapper.toEntity(any(Group.class))).thenReturn(entity);
    when(groupRepository.save(entity)).thenReturn(entity);
    when(groupMapper.toDomain(entity)).thenReturn(saved);

    var result = groupService.create(request);

    assertEquals("K1", result.getRef());
  }

  @Test
  void shouldRejectDuplicateGroup() {
    CreateGroupRequest request = new CreateGroupRequest();
    request.setRef("K1");

    when(groupRepository.existsByRefIgnoreCase("K1")).thenReturn(true);

    assertThrows(ConflictException.class, () -> groupService.create(request));
  }

  @Test
  void shouldUpdateGroup() {
    UUID id = UUID.randomUUID();
    GroupEntity entity = new GroupEntity();

    Group current = Group.builder().id(id).ref("K1").build();

    Group updated = Group.builder().id(id).ref("K2").build();

    UpdateGroupRequest request = new UpdateGroupRequest();
    request.setRef("K2");

    when(groupRepository.findById(id)).thenReturn(Optional.of(entity));
    when(groupMapper.toDomain(entity)).thenReturn(current, updated);
    when(groupRepository.existsByRefIgnoreCaseAndIdNot("K2", id)).thenReturn(false);
    when(groupMapper.toEntity(any(Group.class))).thenReturn(entity);
    when(groupRepository.save(entity)).thenReturn(entity);

    var result = groupService.update(id, request);

    assertEquals("K2", result.getRef());
  }

  @Test
  void shouldRejectDuplicateGroupOnUpdate() {
    UUID id = UUID.randomUUID();
    GroupEntity entity = new GroupEntity();

    Group current = Group.builder().id(id).ref("K1").build();

    UpdateGroupRequest request = new UpdateGroupRequest();
    request.setRef("K2");

    when(groupRepository.findById(id)).thenReturn(Optional.of(entity));
    when(groupMapper.toDomain(entity)).thenReturn(current);
    when(groupRepository.existsByRefIgnoreCaseAndIdNot("K2", id)).thenReturn(true);

    assertThrows(ConflictException.class, () -> groupService.update(id, request));
  }

  @Test
  void shouldRejectUnknownGroupOnUpdate() {
    UUID id = UUID.randomUUID();

    when(groupRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> groupService.update(id, new UpdateGroupRequest()));
  }
}
