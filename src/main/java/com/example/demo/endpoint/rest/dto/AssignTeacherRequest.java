package com.example.demo.endpoint.rest.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignTeacherRequest(@NotNull UUID teacherId) {}
