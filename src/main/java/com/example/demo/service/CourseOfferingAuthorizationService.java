package com.example.demo.service;

import com.example.demo.repository.CourseOfferingTeacherRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseOfferingAuthorizationService {

  private final CourseOfferingTeacherRepository courseOfferingTeacherRepository;

  public boolean isTeacherAssigned(UUID teacherId, UUID courseOfferingId) {
    return courseOfferingTeacherRepository.existsByCourseOffering_IdAndTeacher_Id(
        courseOfferingId, teacherId);
  }
}
