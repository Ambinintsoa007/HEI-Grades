package com.example.demo.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.conf.SecurityConf;
import com.example.demo.endpoint.rest.dto.UserResponse;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;
import com.example.demo.service.UserService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(SecurityConf.class)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private UserService userService;

  @MockBean private JwtDecoder jwtDecoder;

  @Test
  void adminShouldCreateUser() throws Exception {
    when(userService.create(any()))
        .thenReturn(
            UserResponse.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@hei.school")
                .role(UserRole.TEACHER)
                .status(UserStatus.ACTIVE)
                .build());

    mockMvc
        .perform(
            post("/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "firstName": "John",
                      "lastName": "Doe",
                      "email": "john@hei.school",
                      "password": "password",
                      "role": "TEACHER"
                    }
                    """))
        .andExpect(status().isCreated());
  }

  @Test
  void teacherShouldNotCreateUser() throws Exception {
    mockMvc
        .perform(
            post("/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TEACHER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "firstName": "John",
                      "lastName": "Doe",
                      "email": "john@hei.school",
                      "password": "password",
                      "role": "TEACHER"
                    }
                    """))
        .andExpect(status().isForbidden());
  }

  @Test
  void studentShouldNotCreateUser() throws Exception {
    mockMvc
        .perform(
            post("/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "firstName": "John",
                      "lastName": "Doe",
                      "email": "john@hei.school",
                      "password": "password",
                      "role": "TEACHER"
                    }
                    """))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminShouldUpdateUserStatus() throws Exception {
    UUID userId = UUID.randomUUID();

    when(userService.updateStatus(any(), any()))
        .thenReturn(
            UserResponse.builder()
                .id(userId)
                .role(UserRole.TEACHER)
                .status(UserStatus.DISABLED)
                .build());

    mockMvc
        .perform(
            patch("/users/{userId}/status", userId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "status": "DISABLED"
                    }
                    """))
        .andExpect(status().isOk());
  }
}
