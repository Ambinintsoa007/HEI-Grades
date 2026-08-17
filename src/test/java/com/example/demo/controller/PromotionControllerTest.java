package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.conf.SecurityConf;
import com.example.demo.endpoint.rest.controller.PromotionController;
import com.example.demo.endpoint.rest.dto.PromotionResponse;
import com.example.demo.service.PromotionService;
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

@WebMvcTest(PromotionController.class)
@Import(SecurityConf.class)
class PromotionControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PromotionService promotionService;
  @MockBean private JwtDecoder jwtDecoder;

  @Test
  void authenticatedUserShouldGetPromotions() throws Exception {
    when(promotionService.getAll()).thenReturn(List.of());

    mockMvc.perform(get("/promotions").with(jwt())).andExpect(status().isOk());
  }

  @Test
  void adminShouldCreatePromotion() throws Exception {
    when(promotionService.create(any()))
        .thenReturn(
            PromotionResponse.builder()
                .id(UUID.randomUUID())
                .name("2025")
                .startYear(2025)
                .endYear(2028)
                .build());

    mockMvc
        .perform(
            post("/promotions")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "2025",
                      "startYear": 2025,
                      "endYear": 2028
                    }
                    """))
        .andExpect(status().isCreated());
  }

  @Test
  void teacherShouldNotCreatePromotion() throws Exception {
    mockMvc
        .perform(
            post("/promotions")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TEACHER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "2025",
                      "startYear": 2025,
                      "endYear": 2028
                    }
                    """))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminShouldUpdatePromotion() throws Exception {
    UUID promotionId = UUID.randomUUID();

    when(promotionService.update(any(), any()))
        .thenReturn(
            PromotionResponse.builder()
                .id(promotionId)
                .name("Promotion 2025")
                .startYear(2025)
                .endYear(2028)
                .build());

    mockMvc
        .perform(
            patch("/promotions/{promotionId}", promotionId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Promotion 2025"
                    }
                    """))
        .andExpect(status().isOk());
  }
}
