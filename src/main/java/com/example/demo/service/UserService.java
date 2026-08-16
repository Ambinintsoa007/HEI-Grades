package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.CreateUserRequest;
import com.example.demo.endpoint.rest.dto.UpdateUserStatusRequest;
import com.example.demo.endpoint.rest.dto.UserResponse;
import com.example.demo.endpoint.rest.exception.ApiException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PromotionRepository promotionRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public UserResponse create(CreateUserRequest request) {
    validateCreation(request);

    User user =
        User.builder()
            .id(UUID.randomUUID())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .status(UserStatus.ACTIVE)
            .std(request.getRole() == UserRole.STUDENT ? request.getStd() : null)
            .promotionId(request.getRole() == UserRole.STUDENT ? request.getPromotionId() : null)
            .build();

    return toResponse(userMapper.toDomain(userRepository.save(userMapper.toEntity(user))));
  }

  public UserResponse updateStatus(UUID userId, UpdateUserStatusRequest request) {

    User user =
        userRepository
            .findById(userId)
            .map(userMapper::toDomain)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

    User updated =
        User.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .passwordHash(user.getPasswordHash())
            .role(user.getRole())
            .status(request.getStatus())
            .std(user.getStd())
            .promotionId(user.getPromotionId())
            .build();

    return toResponse(userMapper.toDomain(userRepository.save(userMapper.toEntity(updated))));
  }

  private void validateCreation(CreateUserRequest request) {
    if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
      throw new ApiException(HttpStatus.CONFLICT, "Email already exists");
    }

    if (request.getRole() == UserRole.STUDENT) {

      if (request.getStd() == null || request.getStd().isBlank()) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "STD is required for students");
      }

      if (request.getPromotionId() == null) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Promotion is required for students");
      }

      if (userRepository.existsByStdIgnoreCase(request.getStd())) {
        throw new ApiException(HttpStatus.CONFLICT, "STD already exists");
      }

      if (!promotionRepository.existsById(request.getPromotionId())) {
        throw new ApiException(HttpStatus.NOT_FOUND, "Promotion not found");
      }
    }
  }

  private UserResponse toResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .role(user.getRole())
        .status(user.getStatus())
        .std(user.getStd())
        .promotionId(user.getPromotionId())
        .build();
  }
}
