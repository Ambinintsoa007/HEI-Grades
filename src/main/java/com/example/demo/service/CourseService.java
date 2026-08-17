package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.CreateCourseRequest;
import com.example.demo.endpoint.rest.dto.UpdateCourseRequest;
import com.example.demo.endpoint.rest.exception.BusinessException;
import com.example.demo.endpoint.rest.exception.ConflictException;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.model.Course;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.model.CourseEntity;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CourseService {

  private final CourseRepository courseRepository;
  private final CourseMapper courseMapper;

  @Transactional(readOnly = true)
  public List<Course> listCourses() {
    return courseRepository.findAll().stream()
        .map(courseMapper::toDomain)
        .sorted(Comparator.comparing(Course::getRef, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  @Transactional
  public Course createCourse(CreateCourseRequest request) {
    validateRef(request.ref());
    validateTitle(request.title());
    validateCredits(request.credits());
    if (courseRepository.existsByRefIgnoreCase(request.ref())) {
      throw new ConflictException("Course ref already exists: " + request.ref());
    }
    Course course =
        Course.builder()
            .id(UUID.randomUUID())
            .ref(request.ref())
            .title(request.title())
            .credits(request.credits())
            .build();
    return courseMapper.toDomain(courseRepository.save(courseMapper.toEntity(course)));
  }

  @Transactional
  public Course updateCourse(UUID courseId, UpdateCourseRequest request) {
    CourseEntity entity =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
    if (request.ref() != null) {
      validateRef(request.ref());
      if (courseRepository.existsByRefIgnoreCase(request.ref())
          && !entity.getRef().equalsIgnoreCase(request.ref())) {
        throw new ConflictException("Course ref already exists: " + request.ref());
      }
      entity.setRef(request.ref());
    }
    if (request.title() != null) {
      validateTitle(request.title());
      entity.setTitle(request.title());
    }
    if (request.credits() != null) {
      validateCredits(request.credits());
      entity.setCredits(request.credits());
    }
    return courseMapper.toDomain(courseRepository.save(entity));
  }

  private void validateRef(String ref) {
    if (ref == null || ref.isBlank()) {
      throw new BusinessException("Course ref must not be blank");
    }
  }

  private void validateTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new BusinessException("Course title must not be blank");
    }
  }

  private void validateCredits(Integer credits) {
    if (credits == null || credits <= 0) {
      throw new BusinessException("Course credits must be greater than 0");
    }
  }
}
