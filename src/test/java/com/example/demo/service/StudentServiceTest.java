package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.endpoint.rest.dto.StudentGroupAssignmentRequest;
import com.example.demo.endpoint.rest.dto.UpdateStudentRequest;
import com.example.demo.endpoint.rest.exception.ConflictException;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.mapper.StudentCourseEnrollmentMapper;
import com.example.demo.mapper.StudentGroupHistoryMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.repository.model.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StudentServiceTest {

  private UserRepository userRepository;
  private PromotionRepository promotionRepository;
  private UserMapper userMapper;
  private StudentService studentService;

  private AcademicYearRepository academicYearRepository;
  private GroupRepository groupRepository;
  private StudentGroupHistoryRepository studentGroupHistoryRepository;
  private StudentCourseEnrollmentRepository studentCourseEnrollmentRepository;
  private CourseOfferingRepository courseOfferingRepository;
  private StudentGroupHistoryMapper studentGroupHistoryMapper;
  private StudentCourseEnrollmentMapper studentCourseEnrollmentMapper;
  private CourseRepository courseRepository;
  private CourseMapper courseMapper;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    promotionRepository = mock(PromotionRepository.class);
    userMapper = mock(UserMapper.class);

    academicYearRepository = mock(AcademicYearRepository.class);
    groupRepository = mock(GroupRepository.class);
    studentGroupHistoryRepository = mock(StudentGroupHistoryRepository.class);
    studentCourseEnrollmentRepository = mock(StudentCourseEnrollmentRepository.class);
    courseOfferingRepository = mock(CourseOfferingRepository.class);
    studentGroupHistoryMapper = mock(StudentGroupHistoryMapper.class);
    studentCourseEnrollmentMapper = mock(StudentCourseEnrollmentMapper.class);
    courseRepository = mock(CourseRepository.class);
    courseMapper = mock(CourseMapper.class);

    studentService =
        new StudentService(
            userRepository,
            promotionRepository,
            userMapper,
            academicYearRepository,
            groupRepository,
            studentGroupHistoryRepository,
            studentCourseEnrollmentRepository,
            courseOfferingRepository,
            studentGroupHistoryMapper,
            studentCourseEnrollmentMapper,
            courseRepository,
            courseMapper);
  }

  @Test
  void shouldGetStudent() {
    UUID studentId = UUID.randomUUID();
    UUID promotionId = UUID.randomUUID();
    UserEntity entity = new UserEntity();

    User student = student(studentId, promotionId);

    when(userRepository.findById(studentId)).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(student);

    var result = studentService.getStudent(studentId);

    assertEquals(studentId, result.getId());
    assertEquals(UserRole.STUDENT, result.getRole());
    assertEquals("STD25001", result.getStd());
  }

  @Test
  void shouldRejectNonStudent() {
    UUID id = UUID.randomUUID();
    UserEntity entity = new UserEntity();

    User teacher = User.builder().id(id).role(UserRole.TEACHER).status(UserStatus.ACTIVE).build();

    when(userRepository.findById(id)).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(teacher);

    assertThrows(ResourceNotFoundException.class, () -> studentService.getStudent(id));
  }

  @Test
  void shouldUpdateStudent() {
    UUID studentId = UUID.randomUUID();
    UUID promotionId = UUID.randomUUID();
    UserEntity entity = new UserEntity();

    User current = student(studentId, promotionId);

    User updated =
        User.builder()
            .id(studentId)
            .firstName("Updated")
            .lastName(current.getLastName())
            .email(current.getEmail())
            .passwordHash(current.getPasswordHash())
            .role(UserRole.STUDENT)
            .status(UserStatus.ACTIVE)
            .std(current.getStd())
            .promotionId(promotionId)
            .build();

    UpdateStudentRequest request = new UpdateStudentRequest();
    request.setFirstName("Updated");

    when(userRepository.findById(studentId)).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(current, updated);
    when(promotionRepository.existsById(promotionId)).thenReturn(true);
    when(userMapper.toEntity(any(User.class))).thenReturn(entity);
    when(userRepository.save(entity)).thenReturn(entity);

    var result = studentService.update(studentId, request);

    assertEquals("Updated", result.getFirstName());
  }

  @Test
  void shouldRejectDuplicateEmail() {
    UUID studentId = UUID.randomUUID();
    UUID promotionId = UUID.randomUUID();
    UserEntity entity = new UserEntity();

    User current = student(studentId, promotionId);

    UpdateStudentRequest request = new UpdateStudentRequest();
    request.setEmail("existing@hei.school");

    when(userRepository.findById(studentId)).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(current);
    when(userRepository.existsByEmailIgnoreCase("existing@hei.school")).thenReturn(true);

    assertThrows(ConflictException.class, () -> studentService.update(studentId, request));
  }

  @Test
  void shouldRejectDuplicateStd() {
    UUID studentId = UUID.randomUUID();
    UUID promotionId = UUID.randomUUID();
    UserEntity entity = new UserEntity();

    User current = student(studentId, promotionId);

    UpdateStudentRequest request = new UpdateStudentRequest();
    request.setStd("STD99999");

    when(userRepository.findById(studentId)).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(current);
    when(userRepository.existsByStdIgnoreCase("STD99999")).thenReturn(true);
    when(promotionRepository.existsById(promotionId)).thenReturn(true);

    assertThrows(ConflictException.class, () -> studentService.update(studentId, request));
  }

  @Test
  void shouldRejectUnknownPromotion() {
    UUID studentId = UUID.randomUUID();
    UUID currentPromotionId = UUID.randomUUID();
    UUID newPromotionId = UUID.randomUUID();
    UserEntity entity = new UserEntity();

    User current = student(studentId, currentPromotionId);

    UpdateStudentRequest request = new UpdateStudentRequest();
    request.setPromotionId(newPromotionId);

    when(userRepository.findById(studentId)).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(entity)).thenReturn(current);
    when(promotionRepository.existsById(newPromotionId)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> studentService.update(studentId, request));
  }

  @Test
  void shouldRejectUnknownAcademicYearWhenAssigningGroup() {
    UUID studentId = UUID.randomUUID();
    UUID promotionId = UUID.randomUUID();

    UserEntity userEntity = new UserEntity();

    when(userRepository.findById(studentId)).thenReturn(Optional.of(userEntity));
    when(userMapper.toDomain(userEntity)).thenReturn(student(studentId, promotionId));

    StudentGroupAssignmentRequest request = new StudentGroupAssignmentRequest();
    request.setAcademicYearId(UUID.randomUUID());
    request.setGroupId(UUID.randomUUID());
    request.setStartedAt(Instant.now());

    when(academicYearRepository.existsById(request.getAcademicYearId())).thenReturn(false);

    assertThrows(
        ResourceNotFoundException.class, () -> studentService.assignGroup(studentId, request));
  }

  @Test
  void shouldRejectUnknownGroupWhenAssigningGroup() {
    UUID studentId = UUID.randomUUID();
    UUID promotionId = UUID.randomUUID();

    UserEntity userEntity = new UserEntity();

    when(userRepository.findById(studentId)).thenReturn(Optional.of(userEntity));
    when(userMapper.toDomain(userEntity)).thenReturn(student(studentId, promotionId));

    StudentGroupAssignmentRequest request = new StudentGroupAssignmentRequest();
    request.setAcademicYearId(UUID.randomUUID());
    request.setGroupId(UUID.randomUUID());
    request.setStartedAt(Instant.now());

    when(academicYearRepository.existsById(request.getAcademicYearId())).thenReturn(true);
    when(groupRepository.existsById(request.getGroupId())).thenReturn(false);

    assertThrows(
        ResourceNotFoundException.class, () -> studentService.assignGroup(studentId, request));
  }

  @Test
  void shouldAssignGroupAndEnrollStudentInCourses() {
    UUID studentId = UUID.randomUUID();
    UUID promotionId = UUID.randomUUID();
    UUID academicYearId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();

    UserEntity userEntity = new UserEntity();
    CourseOfferingEntity offering = mock(CourseOfferingEntity.class);
    CourseEntity courseEntity = mock(CourseEntity.class);

    StudentGroupHistoryEntity historyEntity = mock(StudentGroupHistoryEntity.class);

    StudentCourseEnrollmentEntity enrollmentEntity = mock(StudentCourseEnrollmentEntity.class);

    when(userRepository.findById(studentId)).thenReturn(Optional.of(userEntity));
    when(userMapper.toDomain(userEntity)).thenReturn(student(studentId, promotionId));

    when(academicYearRepository.existsById(academicYearId)).thenReturn(true);
    when(groupRepository.existsById(groupId)).thenReturn(true);

    when(studentGroupHistoryRepository.findByStudent_IdAndEndedAtIsNull(studentId))
        .thenReturn(Optional.empty());

    when(courseOfferingRepository.findByAcademicYear_IdAndGroup_Id(academicYearId, groupId))
        .thenReturn(List.of(offering));

    when(offering.getCourse()).thenReturn(courseEntity);
    when(courseEntity.getId()).thenReturn(courseId);

    when(studentCourseEnrollmentRepository.existsByStudent_IdAndCourse_IdAndAcademicYear_Id(
            studentId, courseId, academicYearId))
        .thenReturn(false);

    when(studentGroupHistoryMapper.toEntity(any(StudentGroupHistory.class)))
        .thenReturn(historyEntity);

    when(studentCourseEnrollmentMapper.toEntity(any(StudentCourseEnrollment.class)))
        .thenReturn(enrollmentEntity);

    StudentGroupAssignmentRequest request = new StudentGroupAssignmentRequest();

    request.setAcademicYearId(academicYearId);
    request.setGroupId(groupId);
    request.setStartedAt(Instant.now());

    studentService.assignGroup(studentId, request);

    verify(studentGroupHistoryRepository).save(historyEntity);
    verify(studentCourseEnrollmentRepository).save(enrollmentEntity);
  }

  @Test
  void shouldNotDuplicateExistingStudentCourseEnrollment() {
    UUID studentId = UUID.randomUUID();
    UUID promotionId = UUID.randomUUID();
    UUID academicYearId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();

    UserEntity userEntity = new UserEntity();
    CourseOfferingEntity offering = mock(CourseOfferingEntity.class);
    CourseEntity courseEntity = mock(CourseEntity.class);

    when(userRepository.findById(studentId)).thenReturn(Optional.of(userEntity));
    when(userMapper.toDomain(userEntity)).thenReturn(student(studentId, promotionId));

    when(academicYearRepository.existsById(academicYearId)).thenReturn(true);
    when(groupRepository.existsById(groupId)).thenReturn(true);

    when(studentGroupHistoryRepository.findByStudent_IdAndEndedAtIsNull(studentId))
        .thenReturn(Optional.empty());

    when(courseOfferingRepository.findByAcademicYear_IdAndGroup_Id(academicYearId, groupId))
        .thenReturn(List.of(offering));

    when(offering.getCourse()).thenReturn(courseEntity);
    when(courseEntity.getId()).thenReturn(courseId);

    when(studentCourseEnrollmentRepository.existsByStudent_IdAndCourse_IdAndAcademicYear_Id(
            studentId, courseId, academicYearId))
        .thenReturn(true);

    when(studentGroupHistoryMapper.toEntity(any()))
        .thenReturn(mock(StudentGroupHistoryEntity.class));

    StudentGroupAssignmentRequest request = new StudentGroupAssignmentRequest();

    request.setAcademicYearId(academicYearId);
    request.setGroupId(groupId);
    request.setStartedAt(Instant.now());

    studentService.assignGroup(studentId, request);

    verify(studentCourseEnrollmentRepository, never()).save(any());
  }

  private User student(UUID id, UUID promotionId) {
    return User.builder()
        .id(id)
        .firstName("Jane")
        .lastName("Doe")
        .email("jane@hei.school")
        .passwordHash("hashed")
        .role(UserRole.STUDENT)
        .status(UserStatus.ACTIVE)
        .std("STD25001")
        .promotionId(promotionId)
        .build();
  }
}
