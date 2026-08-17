package com.example.demo.service;

import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.UserRoleEntity;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminBootstrapService implements CommandLineRunner {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.bootstrap-admin.enabled}")
  private boolean enabled;

  @Value("${app.bootstrap-admin.first-name}")
  private String firstName;

  @Value("${app.bootstrap-admin.last-name}")
  private String lastName;

  @Value("${app.bootstrap-admin.email}")
  private String email;

  @Value("${app.bootstrap-admin.password}")
  private String password;

  @Override
  public void run(String... args) {
    if (!enabled || userRepository.existsByRole(UserRoleEntity.ADMIN)) {
      return;
    }

    validateProperties();

    User admin =
        User.builder()
            .id(UUID.randomUUID())
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .role(UserRole.ADMIN)
            .status(UserStatus.ACTIVE)
            .build();

    userRepository.save(userMapper.toEntity(admin));
  }

  private void validateProperties() {
    if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
      throw new IllegalStateException("Bootstrap ADMIN configuration is incomplete");
    }
  }
}
