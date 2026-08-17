package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.CreateUserRequest;
import com.example.demo.endpoint.rest.dto.UpdateUserStatusRequest;
import com.example.demo.endpoint.rest.dto.UserResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {

    return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
  }

  @PatchMapping("/{userId}/status")
  public ResponseEntity<UserResponse> updateUserStatus(
      @PathVariable UUID userId, @Valid @RequestBody UpdateUserStatusRequest request) {

    return ResponseEntity.ok(userService.updateStatus(userId, request));
  }
}
