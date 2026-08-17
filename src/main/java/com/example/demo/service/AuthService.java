package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.AuthResponse;
import com.example.demo.endpoint.rest.dto.LoginRequest;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.model.UserStatus;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthResponse login(LoginRequest request) {
    User user =
        userRepository
            .findByEmailIgnoreCase(request.getEmail())
            .map(userMapper::toDomain)
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

    if (user.getStatus() == UserStatus.DISABLED) {
      throw new DisabledException("User account is disabled");
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new BadCredentialsException("Invalid credentials");
    }

    String token = jwtService.generateToken(user.getId(), user.getRole().name());

    return AuthResponse.builder().token(token).role(user.getRole()).build();
  }
}
