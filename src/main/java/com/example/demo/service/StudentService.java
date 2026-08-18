package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.StudentCourseResponse;
import com.example.demo.endpoint.rest.dto.StudentGroupAssignmentRequest;
import com.example.demo.endpoint.rest.dto.UpdateStudentRequest;
import com.example.demo.endpoint.rest.dto.UserResponse;
import com.example.demo.endpoint.rest.exception.BusinessException;
import com.example.demo.endpoint.rest.exception.ConflictException;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.mapper.StudentCourseEnrollmentMapper;
import com.example.demo.mapper.StudentGroupHistoryMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.repository.model.CourseOfferingEntity;
import com.example.demo.repository.model.PathwayEntity;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {

  private final UserRepository userRepository;
  private final PromotionRepository promotionRepository;
  private final UserMapper userMapper;

  private final AcademicYearRepository academicYearRepository;
  private final GroupRepository groupRepository;
  private final StudentGroupHistoryRepository studentGroupHistoryRepository;
  private final StudentCourseEnrollmentRepository studentCourseEnrollmentRepository;
  private final CourseOfferingRepository courseOfferingRepository;

  private final StudentGroupHistoryMapper studentGroupHistoryMapper;
  private final StudentCourseEnrollmentMapper studentCourseEnrollmentMapper;

  private final CourseRepository courseRepository;
  private final CourseMapper courseMapper;

  public UserResponse getStudent(UUID studentId) {
    return toResponse(findStudent(studentId));
  }

  public UserResponse update(UUID studentId, UpdateStudentRequest request) {
    User current = findStudent(studentId);

    String email = request.getEmail() != null ? request.getEmail() : current.getEmail();
    String std = request.getStd() != null ? request.getStd() : current.getStd();
    UUID promotionId =
        request.getPromotionId() != null ? request.getPromotionId() : current.getPromotionId();

    if (!email.equalsIgnoreCase(current.getEmail())
        && userRepository.existsByEmailIgnoreCase(email)) {
      throw new ConflictException("Email already exists");
    }

    if (!std.equalsIgnoreCase(current.getStd()) && userRepository.existsByStdIgnoreCase(std)) {
      throw new ConflictException("STD already exists");
    }

    if (!promotionRepository.existsById(promotionId)) {
      throw new ResourceNotFoundException("Promotion not found");
    }

    User updated =
        User.builder()
            .id(current.getId())
            .firstName(
                request.getFirstName() != null ? request.getFirstName() : current.getFirstName())
            .lastName(request.getLastName() != null ? request.getLastName() : current.getLastName())
            .email(email)
            .passwordHash(current.getPasswordHash())
            .role(current.getRole())
            .status(current.getStatus())
            .std(std)
            .promotionId(promotionId)
            .build();

    return toResponse(userMapper.toDomain(userRepository.save(userMapper.toEntity(updated))));
  }

  private User findStudent(UUID studentId) {
    User user =
        userRepository
            .findById(studentId)
            .map(userMapper::toDomain)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

    if (user.getRole() != UserRole.STUDENT) {
      throw new ResourceNotFoundException("Student not found");
    }

    return user;
  }

  @Transactional
  public void assignGroup(UUID studentId, StudentGroupAssignmentRequest request) {

    User student = findStudent(studentId);

    if (!academicYearRepository.existsById(request.getAcademicYearId())) {
      throw new ResourceNotFoundException("Academic year not found");
    }

    if (!groupRepository.existsById(request.getGroupId())) {
      throw new ResourceNotFoundException("Group not found");
    }

    studentGroupHistoryRepository
        .findByStudent_IdAndEndedAtIsNull(studentId)
        .ifPresent(
            current -> {
              if (request.getStartedAt().isBefore(current.getStartedAt())) {
                throw new BusinessException(
                    "New group assignment cannot start before current assignment");
              }

              if (current.getAcademicYear().getId().equals(request.getAcademicYearId())
                  && current.getGroup().getId().equals(request.getGroupId())
                  && samePathway(current.getPathway(), request.getPathway())) {
                throw new ConflictException("Student is already assigned to this group");
              }

              current.setEndedAt(request.getStartedAt());
              studentGroupHistoryRepository.save(current);
            });

    StudentGroupHistory history =
        StudentGroupHistory.builder()
            .id(UUID.randomUUID())
            .studentId(student.getId())
            .academicYearId(request.getAcademicYearId())
            .groupId(request.getGroupId())
            .pathway(request.getPathway())
            .startedAt(request.getStartedAt())
            .build();

    studentGroupHistoryRepository.save(studentGroupHistoryMapper.toEntity(history));

    enrollStudentInGroupCourses(studentId, request);
  }

  private void enrollStudentInGroupCourses(UUID studentId, StudentGroupAssignmentRequest request) {

    var offerings =
        courseOfferingRepository.findByAcademicYear_IdAndGroup_Id(
            request.getAcademicYearId(), request.getGroupId());

    for (CourseOfferingEntity offering : offerings) {
      UUID courseId = offering.getCourse().getId();

      var existingEnrollment =
          studentCourseEnrollmentRepository.findByStudent_IdAndCourse_IdAndAcademicYear_Id(
              studentId, courseId, request.getAcademicYearId());

      if (existingEnrollment.isPresent()) {
        var enrollment = existingEnrollment.get();

        boolean alreadyLinked =
            enrollment.getCourseOfferings().stream()
                .anyMatch(existing -> existing.getId().equals(offering.getId()));

        if (!alreadyLinked) {
          enrollment.getCourseOfferings().add(offering);
          studentCourseEnrollmentRepository.save(enrollment);
        }

        continue;
      }

      StudentCourseEnrollment enrollment =
          StudentCourseEnrollment.builder()
              .id(UUID.randomUUID())
              .studentId(studentId)
              .courseId(courseId)
              .academicYearId(request.getAcademicYearId())
              .courseOfferingIds(Set.of(offering.getId()))
              .enrolledAt(request.getStartedAt())
              .build();

      studentCourseEnrollmentRepository.save(studentCourseEnrollmentMapper.toEntity(enrollment));
    }
  }

  private boolean samePathway(PathwayEntity current, Pathway requested) {
    if (current == null && requested == null) {
      return true;
    }

    if (current == null || requested == null) {
      return false;
    }

    return current.name().equals(requested.name());
  }

  public List<StudentCourseResponse> getCourses(UUID studentId) {
    findStudent(studentId);

    return studentCourseEnrollmentRepository.findAllByStudent_Id(studentId).stream()
        .map(studentCourseEnrollmentMapper::toDomain)
        .map(
            enrollment -> {
              Course course =
                  courseRepository
                      .findById(enrollment.getCourseId())
                      .map(courseMapper::toDomain)
                      .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

              return StudentCourseResponse.builder()
                  .enrollmentId(enrollment.getId())
                  .courseId(course.getId())
                  .ref(course.getRef())
                  .title(course.getTitle())
                  .credits(course.getCredits())
                  .academicYearId(enrollment.getAcademicYearId())
                  .enrolledAt(enrollment.getEnrolledAt())
                  .build();
            })
        .toList();
  }

  private UserResponse toResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .role(user.getRole())
        .status(user.getStatus())
        .std(user.getStd())
        .promotionId(user.getPromotionId())
        .build();
  }
}
