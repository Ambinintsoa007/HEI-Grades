package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.conf.SecurityConf;
import com.example.demo.endpoint.rest.controller.StudentController;
import com.example.demo.endpoint.rest.dto.UserResponse;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;
import com.example.demo.service.StudentService;
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

@WebMvcTest(StudentController.class)
@Import(SecurityConf.class)
class StudentControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private StudentService studentService;
  @MockBean private JwtDecoder jwtDecoder;

  @Test
  void studentShouldGetOwnProfile() throws Exception {
    UUID studentId = UUID.randomUUID();

    when(studentService.getStudent(studentId))
        .thenReturn(
            UserResponse.builder()
                .id(studentId)
                .role(UserRole.STUDENT)
                .status(UserStatus.ACTIVE)
                .std("STD25001")
                .build());

    mockMvc
        .perform(
            get("/students/me")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(studentId.toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
        .andExpect(status().isOk());
  }

  @Test
  void teacherShouldNotGetStudentMe() throws Exception {
    mockMvc
        .perform(
            get("/students/me")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_TEACHER"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminShouldUpdateStudent() throws Exception {
    UUID studentId = UUID.randomUUID();

    when(studentService.update(any(), any()))
        .thenReturn(
            UserResponse.builder()
                .id(studentId)
                .role(UserRole.STUDENT)
                .status(UserStatus.ACTIVE)
                .build());

    mockMvc
        .perform(
            patch("/students/{studentId}", studentId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "firstName": "Updated"
                    }
                    """))
        .andExpect(status().isOk());
  }

  @Test
  void studentShouldNotUpdateStudent() throws Exception {
    mockMvc
        .perform(
            patch("/students/{studentId}", UUID.randomUUID())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "firstName": "Updated"
                    }
                    """))
        .andExpect(status().isForbidden());
  }
}
