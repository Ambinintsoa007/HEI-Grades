package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.conf.SecurityConf;
import com.example.demo.endpoint.rest.controller.GroupController;
import com.example.demo.endpoint.rest.dto.GroupResponse;
import com.example.demo.service.GroupService;
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

@WebMvcTest(GroupController.class)
@Import(SecurityConf.class)
class GroupControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private GroupService groupService;
  @MockBean private JwtDecoder jwtDecoder;

  @Test
  void authenticatedUserShouldGetGroups() throws Exception {
    when(groupService.getAll()).thenReturn(List.of());

    mockMvc.perform(get("/groups").with(jwt())).andExpect(status().isOk());
  }

  @Test
  void adminShouldCreateGroup() throws Exception {
    when(groupService.create(any()))
        .thenReturn(GroupResponse.builder().id(UUID.randomUUID()).ref("K1").build());

    mockMvc
        .perform(
            post("/groups")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "ref": "K1"
                    }
                    """))
        .andExpect(status().isCreated());
  }

  @Test
  void teacherShouldNotCreateGroup() throws Exception {
    mockMvc
        .perform(
            post("/groups")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TEACHER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "ref": "K1"
                    }
                    """))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminShouldUpdateGroup() throws Exception {
    UUID id = UUID.randomUUID();

    when(groupService.update(any(), any()))
        .thenReturn(GroupResponse.builder().id(id).ref("K2").build());

    mockMvc
        .perform(
            patch("/groups/{groupId}", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "ref": "K2"
                    }
                    """))
        .andExpect(status().isOk());
  }
}
