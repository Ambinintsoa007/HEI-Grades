package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.UserEntity;
import com.example.demo.repository.model.UserRoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class AdminBootstrapServiceTest {

  private UserRepository userRepository;
  private UserMapper userMapper;
  private PasswordEncoder passwordEncoder;
  private AdminBootstrapService service;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    userMapper = mock(UserMapper.class);
    passwordEncoder = mock(PasswordEncoder.class);

    service = new AdminBootstrapService(userRepository, userMapper, passwordEncoder);

    ReflectionTestUtils.setField(service, "enabled", true);
    ReflectionTestUtils.setField(service, "firstName", "Admin");
    ReflectionTestUtils.setField(service, "lastName", "HEI");
    ReflectionTestUtils.setField(service, "email", "admin@hei.school");
    ReflectionTestUtils.setField(service, "password", "password");
  }

  @Test
  void shouldCreateAdminWhenNoneExists() {
    UserEntity entity = new UserEntity();

    when(userRepository.existsByRole(UserRoleEntity.ADMIN)).thenReturn(false);
    when(passwordEncoder.encode("password")).thenReturn("hashed-password");
    when(userMapper.toEntity(any())).thenReturn(entity);

    service.run();

    verify(passwordEncoder).encode("password");
    verify(userRepository).save(entity);
  }

  @Test
  void shouldNotCreateAdminWhenAdminAlreadyExists() {
    when(userRepository.existsByRole(UserRoleEntity.ADMIN)).thenReturn(true);

    service.run();

    verify(userRepository, never()).save(any());
  }

  @Test
  void shouldNotCreateAdminWhenBootstrapIsDisabled() {
    ReflectionTestUtils.setField(service, "enabled", false);

    service.run();

    verify(userRepository, never()).existsByRole(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void shouldRejectIncompleteBootstrapConfiguration() {
    ReflectionTestUtils.setField(service, "email", "");

    when(userRepository.existsByRole(UserRoleEntity.ADMIN)).thenReturn(false);

    assertThrows(IllegalStateException.class, () -> service.run());

    verify(userRepository, never()).save(any());
  }
}
