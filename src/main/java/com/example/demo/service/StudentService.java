package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.UpdateStudentRequest;
import com.example.demo.endpoint.rest.dto.UserResponse;
import com.example.demo.endpoint.rest.exception.ApiException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

  private final UserRepository userRepository;
  private final PromotionRepository promotionRepository;
  private final UserMapper userMapper;

  public UserResponse getStudent(UUID studentId) {
    return toResponse(findStudent(studentId));
  }

  public UserResponse update(UUID studentId, UpdateStudentRequest request) {
    User current = findStudent(studentId);

    String email = request.getEmail() != null ? request.getEmail() : current.getEmail();
    String std = request.getStd() != null ? request.getStd() : current.getStd();
    UUID promotionId =
        request.getPromotionId() != null ? request.getPromotionId() : current.getPromotionId();

    if (!email.equalsIgnoreCase(current.getEmail())
        && userRepository.existsByEmailIgnoreCase(email)) {
      throw new ApiException(HttpStatus.CONFLICT, "Email already exists");
    }

    if (!std.equalsIgnoreCase(current.getStd()) && userRepository.existsByStdIgnoreCase(std)) {
      throw new ApiException(HttpStatus.CONFLICT, "STD already exists");
    }

    if (!promotionRepository.existsById(promotionId)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "Promotion not found");
    }

    User updated =
        User.builder()
            .id(current.getId())
            .firstName(
                request.getFirstName() != null ? request.getFirstName() : current.getFirstName())
            .lastName(request.getLastName() != null ? request.getLastName() : current.getLastName())
            .email(email)
            .passwordHash(current.getPasswordHash())
            .role(current.getRole())
            .status(current.getStatus())
            .std(std)
            .promotionId(promotionId)
            .build();

    return toResponse(userMapper.toDomain(userRepository.save(userMapper.toEntity(updated))));
  }

  private User findStudent(UUID studentId) {
    User user =
        userRepository
            .findById(studentId)
            .map(userMapper::toDomain)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));

    if (user.getRole() != UserRole.STUDENT) {
      throw new ApiException(HttpStatus.NOT_FOUND, "Student not found");
    }

    return user;
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
