package com.example.demo.endpoint.rest.dto;

import com.example.demo.model.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserStatusRequest {

  @NotNull private UserStatus status;
}
