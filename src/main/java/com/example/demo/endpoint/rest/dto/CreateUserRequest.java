package com.example.demo.endpoint.rest.dto;

import com.example.demo.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

  @NotBlank private String firstName;
  @NotBlank private String lastName;

  @NotBlank @Email private String email;

  @NotBlank private String password;

  @NotNull private UserRole role;

  private String std;
  private UUID promotionId;
}
