package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.conf.SecurityConf;
import com.example.demo.endpoint.rest.controller.StudentController;
import com.example.demo.endpoint.rest.dto.UserResponse;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;
import com.example.demo.service.StudentAuthorizationService;
import com.example.demo.service.StudentService;
import java.util.List;
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
@Import({SecurityConf.class, StudentAuthorizationService.class})
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

  @Test
  void adminShouldAssignStudentGroup() throws Exception {
    UUID studentId = UUID.randomUUID();

    mockMvc
        .perform(
            post("/students/{studentId}/group-assignments", studentId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "academicYearId": "%s",
                      "groupId": "%s",
                      "pathway": "EL",
                      "startedAt": "2026-01-10T08:00:00Z"
                    }
                    """
                        .formatted(UUID.randomUUID(), UUID.randomUUID())))
        .andExpect(status().isCreated());
  }

  @Test
  void studentShouldNotAssignGroup() throws Exception {
    mockMvc
        .perform(
            post("/students/{studentId}/group-assignments", UUID.randomUUID())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "academicYearId": "%s",
                      "groupId": "%s",
                      "startedAt": "2026-01-10T08:00:00Z"
                    }
                    """
                        .formatted(UUID.randomUUID(), UUID.randomUUID())))
        .andExpect(status().isForbidden());
  }

  @Test
  void studentShouldGetOwnCourses() throws Exception {
    UUID studentId = UUID.randomUUID();

    when(studentService.getCourses(studentId)).thenReturn(List.of());

    mockMvc
        .perform(
            get("/students/{studentId}/courses", studentId)
                .with(
                    jwt()
                        .jwt(token -> token.subject(studentId.toString()).claim("role", "STUDENT"))
                        .authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
        .andExpect(status().isOk());
  }

  @Test
  void studentShouldNotGetAnotherStudentsCourses() throws Exception {
    UUID authenticatedStudentId = UUID.randomUUID();
    UUID otherStudentId = UUID.randomUUID();

    mockMvc
        .perform(
            get("/students/{studentId}/courses", otherStudentId)
                .with(
                    jwt()
                        .jwt(
                            token ->
                                token
                                    .subject(authenticatedStudentId.toString())
                                    .claim("role", "STUDENT"))
                        .authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminShouldGetStudentCourses() throws Exception {
    UUID studentId = UUID.randomUUID();

    when(studentService.getCourses(studentId)).thenReturn(List.of());

    UUID adminId = UUID.randomUUID();

    mockMvc
        .perform(
            get("/students/{studentId}/courses", studentId)
                .with(
                    jwt()
                        .jwt(token -> token.subject(adminId.toString()).claim("role", "ADMIN"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk());
  }

  @Test
  void teacherShouldNotGetStudentCourses() throws Exception {
    mockMvc
        .perform(
            get("/students/{studentId}/courses", UUID.randomUUID())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TEACHER"))))
        .andExpect(status().isForbidden());
  }
}
