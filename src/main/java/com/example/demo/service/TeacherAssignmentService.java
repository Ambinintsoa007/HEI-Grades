package com.example.demo.service;

import com.example.demo.mapper.CourseOfferingTeacherMapper;
import com.example.demo.model.CourseOfferingTeacher;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.CourseOfferingTeacherRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.UserEntity;
import com.example.demo.repository.model.UserRoleEntity;
import com.example.demo.service.exception.BusinessException;
import com.example.demo.service.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TeacherAssignmentService {

  private final CourseOfferingRepository courseOfferingRepository;
  private final UserRepository userRepository;
  private final CourseOfferingTeacherRepository courseOfferingTeacherRepository;
  private final CourseOfferingTeacherMapper courseOfferingTeacherMapper;

  @Transactional
  public CourseOfferingTeacher assignTeacher(UUID courseOfferingId, UUID teacherId) {
    courseOfferingRepository
        .findById(courseOfferingId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Course offering not found: " + courseOfferingId));
    UserEntity teacher =
        userRepository
            .findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + teacherId));
    if (teacher.getRole() != UserRoleEntity.TEACHER) {
      throw new BusinessException("User is not a teacher: " + teacherId);
    }
    if (courseOfferingTeacherRepository.existsByCourseOffering_IdAndTeacher_Id(
        courseOfferingId, teacherId)) {
      throw new BusinessException("Teacher is already assigned to this course offering");
    }
    CourseOfferingTeacher assignment =
        CourseOfferingTeacher.builder()
            .id(UUID.randomUUID())
            .courseOfferingId(courseOfferingId)
            .teacherId(teacherId)
            .build();
    return courseOfferingTeacherMapper.toDomain(
        courseOfferingTeacherRepository.save(courseOfferingTeacherMapper.toEntity(assignment)));
  }
}
