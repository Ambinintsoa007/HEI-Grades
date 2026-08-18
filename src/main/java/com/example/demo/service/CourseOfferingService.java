package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.CreateCourseOfferingRequest;
import com.example.demo.endpoint.rest.exception.ConflictException;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.mapper.CourseOfferingMapper;
import com.example.demo.model.CourseOffering;
import com.example.demo.model.UserRole;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.CourseOfferingTeacherRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.model.CourseOfferingEntity;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CourseOfferingService {

  private final CourseOfferingRepository courseOfferingRepository;
  private final CourseOfferingTeacherRepository courseOfferingTeacherRepository;
  private final CourseRepository courseRepository;
  private final AcademicYearRepository academicYearRepository;
  private final GroupRepository groupRepository;
  private final CourseOfferingMapper courseOfferingMapper;

  @Transactional(readOnly = true)
  public List<CourseOffering> listCourseOfferings(
      UUID authenticatedUserId, UserRole role, UUID academicYearId, UUID groupId) {
    List<CourseOfferingEntity> entities;
    if (academicYearId != null && groupId != null) {
      entities = courseOfferingRepository.findByAcademicYear_IdAndGroup_Id(academicYearId, groupId);
    } else if (academicYearId != null) {
      entities = courseOfferingRepository.findByAcademicYear_Id(academicYearId);
    } else if (groupId != null) {
      entities = courseOfferingRepository.findByGroup_Id(groupId);
    } else {
      entities = courseOfferingRepository.findAll();
    }
    if (role == UserRole.TEACHER) {
      Set<UUID> assignedOfferingIds =
          courseOfferingTeacherRepository.findByTeacher_Id(authenticatedUserId).stream()
              .map(assignment -> assignment.getCourseOffering().getId())
              .collect(Collectors.toSet());
      entities =
          entities.stream().filter(entity -> assignedOfferingIds.contains(entity.getId())).toList();
    }
    return entities.stream()
        .map(courseOfferingMapper::toDomain)
        .sorted(Comparator.comparing(CourseOffering::getId))
        .toList();
  }

  @Transactional
  public CourseOffering createCourseOffering(CreateCourseOfferingRequest request) {
    requireExistingCourse(request.courseId());
    requireExistingAcademicYear(request.academicYearId());
    requireExistingGroup(request.groupId());
    if (courseOfferingRepository
        .findByCourse_IdAndAcademicYear_IdAndGroup_Id(
            request.courseId(), request.academicYearId(), request.groupId())
        .isPresent()) {
      throw new ConflictException(
          "Course offering already exists for this course, academic year and group");
    }
    CourseOffering offering =
        CourseOffering.builder()
            .id(UUID.randomUUID())
            .courseId(request.courseId())
            .academicYearId(request.academicYearId())
            .groupId(request.groupId())
            .build();
    return courseOfferingMapper.toDomain(
        courseOfferingRepository.save(courseOfferingMapper.toEntity(offering)));
  }

  private void requireExistingCourse(UUID courseId) {
    if (!courseRepository.existsById(courseId)) {
      throw new ResourceNotFoundException("Course not found: " + courseId);
    }
  }

  private void requireExistingAcademicYear(UUID academicYearId) {
    if (!academicYearRepository.existsById(academicYearId)) {
      throw new ResourceNotFoundException("Academic year not found: " + academicYearId);
    }
  }

  private void requireExistingGroup(UUID groupId) {
    if (!groupRepository.existsById(groupId)) {
      throw new ResourceNotFoundException("Group not found: " + groupId);
    }
  }
}
