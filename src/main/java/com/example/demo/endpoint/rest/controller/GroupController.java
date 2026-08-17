package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.CreateGroupRequest;
import com.example.demo.endpoint.rest.dto.GroupResponse;
import com.example.demo.endpoint.rest.dto.UpdateGroupRequest;
import com.example.demo.service.GroupService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

  private final GroupService groupService;

  @GetMapping
  public ResponseEntity<List<GroupResponse>> getGroups() {
    return ResponseEntity.ok(groupService.getAll());
  }

  @PostMapping
  public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {

    return ResponseEntity.status(HttpStatus.CREATED).body(groupService.create(request));
  }

  @PatchMapping("/{groupId}")
  public ResponseEntity<GroupResponse> updateGroup(
      @PathVariable UUID groupId, @RequestBody UpdateGroupRequest request) {

    return ResponseEntity.ok(groupService.update(groupId, request));
  }
}
