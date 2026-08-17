package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.CreateGroupRequest;
import com.example.demo.endpoint.rest.dto.GroupResponse;
import com.example.demo.endpoint.rest.dto.UpdateGroupRequest;
import com.example.demo.endpoint.rest.exception.ApiException;
import com.example.demo.mapper.GroupMapper;
import com.example.demo.model.Group;
import com.example.demo.repository.GroupRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;
  private final GroupMapper groupMapper;

  public List<GroupResponse> getAll() {
    return groupRepository.findAll().stream()
        .map(groupMapper::toDomain)
        .map(this::toResponse)
        .toList();
  }

  public GroupResponse create(CreateGroupRequest request) {
    if (groupRepository.existsByRefIgnoreCase(request.getRef())) {
      throw new ApiException(HttpStatus.CONFLICT, "Group already exists");
    }

    Group group = Group.builder().id(UUID.randomUUID()).ref(request.getRef()).build();

    return toResponse(groupMapper.toDomain(groupRepository.save(groupMapper.toEntity(group))));
  }

  public GroupResponse update(UUID groupId, UpdateGroupRequest request) {
    Group current =
        groupRepository
            .findById(groupId)
            .map(groupMapper::toDomain)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Group not found"));

    String ref = request.getRef() != null ? request.getRef() : current.getRef();

    if (groupRepository.existsByRefIgnoreCaseAndIdNot(ref, groupId)) {
      throw new ApiException(HttpStatus.CONFLICT, "Group already exists");
    }

    Group updated = Group.builder().id(current.getId()).ref(ref).build();

    return toResponse(groupMapper.toDomain(groupRepository.save(groupMapper.toEntity(updated))));
  }

  private GroupResponse toResponse(Group group) {
    return GroupResponse.builder().id(group.getId()).ref(group.getRef()).build();
  }
}
