package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.endpoint.rest.dto.LoginRequest;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

  private UserRepository userRepository;
  private UserMapper userMapper;
  private PasswordEncoder passwordEncoder;
  private JwtService jwtService;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    userMapper = mock(UserMapper.class);
    passwordEncoder = mock(PasswordEncoder.class);
    jwtService = mock(JwtService.class);

    authService = new AuthService(userRepository, userMapper, passwordEncoder, jwtService);
  }

  @Test
  void shouldLoginActiveUser() {
    UUID userId = UUID.randomUUID();

    LoginRequest request = new LoginRequest();
    request.setEmail("admin@hei.school");
    request.setPassword("password");

    UserEntity entity = new UserEntity();

    User user =
        User.builder()
            .id(userId)
            .email("admin@hei.school")
            .passwordHash("hashed-password")
            .role(UserRole.ADMIN)
            .status(UserStatus.ACTIVE)
            .build();

    when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(user);
    when(passwordEncoder.matches("password", "hashed-password")).thenReturn(true);
    when(jwtService.generateToken(userId, "ADMIN")).thenReturn("jwt-token");

    var response = authService.login(request);

    assertEquals("jwt-token", response.getToken());
    assertEquals(UserRole.ADMIN, response.getRole());
  }

  @Test
  void shouldRejectUnknownEmail() {
    LoginRequest request = new LoginRequest();
    request.setEmail("unknown@hei.school");
    request.setPassword("password");

    when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.empty());

    assertThrows(BadCredentialsException.class, () -> authService.login(request));
  }

  @Test
  void shouldRejectWrongPassword() {
    LoginRequest request = new LoginRequest();
    request.setEmail("teacher@hei.school");
    request.setPassword("wrong");

    UserEntity entity = new UserEntity();

    User user =
        User.builder()
            .id(UUID.randomUUID())
            .passwordHash("hashed-password")
            .role(UserRole.TEACHER)
            .status(UserStatus.ACTIVE)
            .build();

    when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(user);
    when(passwordEncoder.matches("wrong", "hashed-password")).thenReturn(false);

    assertThrows(BadCredentialsException.class, () -> authService.login(request));
  }

  @Test
  void shouldRejectDisabledUser() {
    LoginRequest request = new LoginRequest();
    request.setEmail("student@hei.school");
    request.setPassword("password");

    UserEntity entity = new UserEntity();

    User user =
        User.builder()
            .id(UUID.randomUUID())
            .passwordHash("hashed-password")
            .role(UserRole.STUDENT)
            .status(UserStatus.DISABLED)
            .build();

    when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(user);

    assertThrows(DisabledException.class, () -> authService.login(request));

    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }
}
