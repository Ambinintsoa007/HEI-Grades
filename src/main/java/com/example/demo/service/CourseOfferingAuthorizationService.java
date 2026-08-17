package com.example.demo.service;

import com.example.demo.model.UserRole;
import com.example.demo.repository.CourseOfferingTeacherRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseOfferingAuthorizationService {

  private final CourseOfferingTeacherRepository courseOfferingTeacherRepository;

  public void checkCanAccessCourseOffering(
      UUID authenticatedUserId, UserRole role, UUID courseOfferingId) {
    if (role == UserRole.ADMIN) {
      return;
    }
    if (role == UserRole.TEACHER
        && courseOfferingTeacherRepository.existsByCourseOffering_IdAndTeacher_Id(
            courseOfferingId, authenticatedUserId)) {
      return;
    }
    throw new AccessDeniedException("Access denied");
  }

  public boolean isTeacherAssigned(UUID teacherId, UUID courseOfferingId) {
    return courseOfferingTeacherRepository.existsByCourseOffering_IdAndTeacher_Id(
        courseOfferingId, teacherId);
  }
}
