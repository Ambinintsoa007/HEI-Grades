package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.endpoint.rest.dto.CreateUserRequest;
import com.example.demo.endpoint.rest.dto.UpdateUserStatusRequest;
import com.example.demo.endpoint.rest.exception.BusinessException;
import com.example.demo.endpoint.rest.exception.ConflictException;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

  private UserRepository userRepository;
  private PromotionRepository promotionRepository;
  private UserMapper userMapper;
  private PasswordEncoder passwordEncoder;
  private UserService userService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    promotionRepository = mock(PromotionRepository.class);
    userMapper = mock(UserMapper.class);
    passwordEncoder = mock(PasswordEncoder.class);

    userService = new UserService(userRepository, promotionRepository, userMapper, passwordEncoder);
  }

  @Test
  void shouldCreateTeacher() {
    CreateUserRequest request = new CreateUserRequest();
    request.setFirstName("John");
    request.setLastName("Doe");
    request.setEmail("john@hei.school");
    request.setPassword("password");
    request.setRole(UserRole.TEACHER);

    UserEntity entity = new UserEntity();

    User savedUser =
        User.builder()
            .id(UUID.randomUUID())
            .firstName("John")
            .lastName("Doe")
            .email("john@hei.school")
            .passwordHash("hashed")
            .role(UserRole.TEACHER)
            .status(UserStatus.ACTIVE)
            .build();

    when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
    when(passwordEncoder.encode("password")).thenReturn("hashed");
    when(userMapper.toEntity(any(User.class))).thenReturn(entity);
    when(userRepository.save(entity)).thenReturn(entity);
    when(userMapper.toDomain(entity)).thenReturn(savedUser);

    var response = userService.create(request);

    assertEquals(UserRole.TEACHER, response.getRole());
    assertEquals(UserStatus.ACTIVE, response.getStatus());
    assertNull(response.getStd());
    assertNull(response.getPromotionId());

    verify(userRepository).save(entity);
  }

  @Test
  void shouldCreateStudent() {
    UUID promotionId = UUID.randomUUID();

    CreateUserRequest request = new CreateUserRequest();
    request.setFirstName("Jane");
    request.setLastName("Doe");
    request.setEmail("jane@hei.school");
    request.setPassword("password");
    request.setRole(UserRole.STUDENT);
    request.setStd("STD25001");
    request.setPromotionId(promotionId);

    UserEntity entity = new UserEntity();

    User savedUser =
        User.builder()
            .id(UUID.randomUUID())
            .firstName("Jane")
            .lastName("Doe")
            .email("jane@hei.school")
            .passwordHash("hashed")
            .role(UserRole.STUDENT)
            .status(UserStatus.ACTIVE)
            .std("STD25001")
            .promotionId(promotionId)
            .build();

    when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
    when(userRepository.existsByStdIgnoreCase("STD25001")).thenReturn(false);
    when(promotionRepository.existsById(promotionId)).thenReturn(true);
    when(passwordEncoder.encode("password")).thenReturn("hashed");
    when(userMapper.toEntity(any(User.class))).thenReturn(entity);
    when(userRepository.save(entity)).thenReturn(entity);
    when(userMapper.toDomain(entity)).thenReturn(savedUser);

    var response = userService.create(request);

    assertEquals(UserRole.STUDENT, response.getRole());
    assertEquals("STD25001", response.getStd());
    assertEquals(promotionId, response.getPromotionId());
  }

  @Test
  void shouldRejectDuplicateEmail() {
    CreateUserRequest request = new CreateUserRequest();
    request.setEmail("existing@hei.school");

    when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(true);

    assertThrows(ConflictException.class, () -> userService.create(request));
  }

  @Test
  void shouldRejectStudentWithoutStd() {
    CreateUserRequest request = validStudentRequest();
    request.setStd(null);

    assertThrows(BusinessException.class, () -> userService.create(request));
  }

  @Test
  void shouldRejectStudentWithoutPromotion() {
    CreateUserRequest request = validStudentRequest();
    request.setPromotionId(null);

    assertThrows(BusinessException.class, () -> userService.create(request));
  }

  @Test
  void shouldRejectDuplicateStd() {
    CreateUserRequest request = validStudentRequest();

    when(userRepository.existsByStdIgnoreCase(request.getStd())).thenReturn(true);

    assertThrows(ConflictException.class, () -> userService.create(request));
  }

  @Test
  void shouldRejectUnknownPromotion() {
    CreateUserRequest request = validStudentRequest();

    when(userRepository.existsByStdIgnoreCase(request.getStd())).thenReturn(false);
    when(promotionRepository.existsById(request.getPromotionId())).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> userService.create(request));
  }

  @Test
  void shouldUpdateUserStatus() {
    UUID userId = UUID.randomUUID();

    UpdateUserStatusRequest request = new UpdateUserStatusRequest();
    request.setStatus(UserStatus.DISABLED);

    UserEntity entity = new UserEntity();

    User user =
        User.builder()
            .id(userId)
            .email("user@hei.school")
            .role(UserRole.TEACHER)
            .status(UserStatus.ACTIVE)
            .build();

    User updated =
        User.builder()
            .id(userId)
            .email("user@hei.school")
            .role(UserRole.TEACHER)
            .status(UserStatus.DISABLED)
            .build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(user, updated);
    when(userMapper.toEntity(any(User.class))).thenReturn(entity);
    when(userRepository.save(entity)).thenReturn(entity);

    var response = userService.updateStatus(userId, request);

    assertEquals(UserStatus.DISABLED, response.getStatus());
  }

  @Test
  void shouldRejectUnknownUserWhenUpdatingStatus() {
    UUID userId = UUID.randomUUID();

    UpdateUserStatusRequest request = new UpdateUserStatusRequest();
    request.setStatus(UserStatus.DISABLED);

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> userService.updateStatus(userId, request));
  }

  private CreateUserRequest validStudentRequest() {
    CreateUserRequest request = new CreateUserRequest();
    request.setFirstName("Jane");
    request.setLastName("Doe");
    request.setEmail("jane@hei.school");
    request.setPassword("password");
    request.setRole(UserRole.STUDENT);
    request.setStd("STD25001");
    request.setPromotionId(UUID.randomUUID());

    return request;
  }
}
