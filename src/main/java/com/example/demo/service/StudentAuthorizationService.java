package com.example.demo.service;

import com.example.demo.model.UserRole;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class StudentAuthorizationService {

  public void checkCanAccessStudent(UUID authenticatedUserId, UserRole role, UUID targetStudentId) {

    if (role == UserRole.STUDENT && !authenticatedUserId.equals(targetStudentId)) {
      throw new AccessDeniedException("Access denied");
    }
  }
}
