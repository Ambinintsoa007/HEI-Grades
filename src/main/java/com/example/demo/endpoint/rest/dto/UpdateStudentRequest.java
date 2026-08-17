package com.example.demo.endpoint.rest.dto;

import jakarta.validation.constraints.Email;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStudentRequest {

  private String firstName;
  private String lastName;

  @Email private String email;

  private String std;
  private UUID promotionId;
}
