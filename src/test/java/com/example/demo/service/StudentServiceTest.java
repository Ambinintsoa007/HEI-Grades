package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.endpoint.rest.dto.UpdateStudentRequest;
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

class StudentServiceTest {

  private UserRepository userRepository;
  private PromotionRepository promotionRepository;
  private UserMapper userMapper;
  private StudentService studentService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    promotionRepository = mock(PromotionRepository.class);
    userMapper = mock(UserMapper.class);

    studentService = new StudentService(userRepository, promotionRepository, userMapper);
  }

  @Test
  void shouldGetStudent() {
    UUID studentId = UUID.randomUUID();
    UUID promotionId = UUID.randomUUID();
    UserEntity entity = new UserEntity();

    User student = student(studentId, promotionId);

    when(userRepository.findById(studentId)).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(student);

    var result = studentService.getStudent(studentId);

    assertEquals(studentId, result.getId());
    assertEquals(UserRole.STUDENT, result.getRole());
    assertEquals("STD25001", result.getStd());
  }

  @Test
  void shouldRejectNonStudent() {
    UUID id = UUID.randomUUID();
    UserEntity entity = new UserEntity();

    User teacher = User.builder().id(id).role(UserRole.TEACHER).status(UserStatus.ACTIVE).build();

    when(userRepository.findById(id)).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(teacher);

    assertThrows(ResourceNotFoundException.class, () -> studentService.getStudent(id));
  }

  @Test
  void shouldUpdateStudent() {
    UUID studentId = UUID.randomUUID();
    UUID promotionId = UUID.randomUUID();
    UserEntity entity = new UserEntity();

    User current = student(studentId, promotionId);

    User updated =
        User.builder()
            .id(studentId)
            .firstName("Updated")
            .lastName(current.getLastName())
            .email(current.getEmail())
            .passwordHash(current.getPasswordHash())
            .role(UserRole.STUDENT)
            .status(UserStatus.ACTIVE)
            .std(current.getStd())
            .promotionId(promotionId)
            .build();

    UpdateStudentRequest request = new UpdateStudentRequest();
    request.setFirstName("Updated");

    when(userRepository.findById(studentId)).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(current, updated);
    when(promotionRepository.existsById(promotionId)).thenReturn(true);
    when(userMapper.toEntity(any(User.class))).thenReturn(entity);
    when(userRepository.save(entity)).thenReturn(entity);

    var result = studentService.update(studentId, request);

    assertEquals("Updated", result.getFirstName());
  }

  @Test
  void shouldRejectDuplicateEmail() {
    UUID studentId = UUID.randomUUID();
    UUID promotionId = UUID.randomUUID();
    UserEntity entity = new UserEntity();

    User current = student(studentId, promotionId);

    UpdateStudentRequest request = new UpdateStudentRequest();
    request.setEmail("existing@hei.school");

    when(userRepository.findById(studentId)).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(current);
    when(userRepository.existsByEmailIgnoreCase("existing@hei.school")).thenReturn(true);

    assertThrows(ConflictException.class, () -> studentService.update(studentId, request));
  }

  @Test
  void shouldRejectDuplicateStd() {
    UUID studentId = UUID.randomUUID();
    UUID promotionId = UUID.randomUUID();
    UserEntity entity = new UserEntity();

    User current = student(studentId, promotionId);

    UpdateStudentRequest request = new UpdateStudentRequest();
    request.setStd("STD99999");

    when(userRepository.findById(studentId)).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(current);
    when(userRepository.existsByStdIgnoreCase("STD99999")).thenReturn(true);
    when(promotionRepository.existsById(promotionId)).thenReturn(true);

    assertThrows(ConflictException.class, () -> studentService.update(studentId, request));
  }

  @Test
  void shouldRejectUnknownPromotion() {
    UUID studentId = UUID.randomUUID();
    UUID currentPromotionId = UUID.randomUUID();
    UUID newPromotionId = UUID.randomUUID();
    UserEntity entity = new UserEntity();

    User current = student(studentId, currentPromotionId);

    UpdateStudentRequest request = new UpdateStudentRequest();
    request.setPromotionId(newPromotionId);

    when(userRepository.findById(studentId)).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(current);
    when(promotionRepository.existsById(newPromotionId)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> studentService.update(studentId, request));
  }

  private User student(UUID id, UUID promotionId) {
    return User.builder()
        .id(id)
        .firstName("Jane")
        .lastName("Doe")
        .email("jane@hei.school")
        .passwordHash("hashed")
        .role(UserRole.STUDENT)
        .status(UserStatus.ACTIVE)
        .std("STD25001")
        .promotionId(promotionId)
        .build();
  }
}
