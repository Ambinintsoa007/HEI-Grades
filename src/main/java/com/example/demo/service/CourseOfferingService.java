package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.CreateCourseOfferingRequest;
import com.example.demo.mapper.CourseOfferingMapper;
import com.example.demo.model.CourseOffering;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.model.CourseOfferingEntity;
import com.example.demo.service.exception.BusinessException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CourseOfferingService {

  private final CourseOfferingRepository courseOfferingRepository;
  private final CourseRepository courseRepository;
  private final AcademicYearRepository academicYearRepository;
  private final GroupRepository groupRepository;
  private final CourseOfferingMapper courseOfferingMapper;

  @Transactional(readOnly = true)
  public List<CourseOffering> listCourseOfferings(UUID academicYearId, UUID groupId) {
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
      throw new BusinessException(
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
      throw new BusinessException("Course not found: " + courseId);
    }
  }

  private void requireExistingAcademicYear(UUID academicYearId) {
    if (!academicYearRepository.existsById(academicYearId)) {
      throw new BusinessException("Academic year not found: " + academicYearId);
    }
  }

  private void requireExistingGroup(UUID groupId) {
    if (!groupRepository.existsById(groupId)) {
      throw new BusinessException("Group not found: " + groupId);
    }
  }
}
