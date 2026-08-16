package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.conf.SecurityConf;
import com.example.demo.endpoint.rest.controller.AcademicYearController;
import com.example.demo.endpoint.rest.dto.AcademicYearResponse;
import com.example.demo.service.AcademicYearService;
import java.time.LocalDate;
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

@WebMvcTest(AcademicYearController.class)
@Import(SecurityConf.class)
class AcademicYearControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AcademicYearService academicYearService;
  @MockBean private JwtDecoder jwtDecoder;

  @Test
  void authenticatedUserShouldGetAcademicYears() throws Exception {
    when(academicYearService.getAll()).thenReturn(List.of());

    mockMvc.perform(get("/academic-years").with(jwt())).andExpect(status().isOk());
  }

  @Test
  void adminShouldCreateAcademicYear() throws Exception {
    when(academicYearService.create(any()))
        .thenReturn(
            AcademicYearResponse.builder()
                .id(UUID.randomUUID())
                .label("2025-2026")
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2026, 7, 31))
                .build());

    mockMvc
        .perform(
            post("/academic-years")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "label": "2025-2026",
                      "startDate": "2025-09-01",
                      "endDate": "2026-07-31"
                    }
                    """))
        .andExpect(status().isCreated());
  }

  @Test
  void teacherShouldNotCreateAcademicYear() throws Exception {
    mockMvc
        .perform(
            post("/academic-years")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TEACHER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "label": "2025-2026",
                      "startDate": "2025-09-01",
                      "endDate": "2026-07-31"
                    }
                    """))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminShouldUpdateAcademicYear() throws Exception {
    UUID id = UUID.randomUUID();

    when(academicYearService.update(any(), any()))
        .thenReturn(
            AcademicYearResponse.builder()
                .id(id)
                .label("2025/2026")
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2026, 7, 31))
                .build());

    mockMvc
        .perform(
            patch("/academic-years/{id}", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "label": "2025/2026"
                    }
                    """))
        .andExpect(status().isOk());
  }
}
