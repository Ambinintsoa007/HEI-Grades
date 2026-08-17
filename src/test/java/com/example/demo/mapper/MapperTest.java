package com.example.demo.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.model.*;
import com.example.demo.repository.model.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MapperTest {

  @Test
  void shouldMapAcademicYear() {
    UUID id = UUID.randomUUID();

    AcademicYearMapper mapper = new AcademicYearMapper();

    AcademicYearEntity entity =
        AcademicYearEntity.builder()
            .id(id)
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 7, 31))
            .build();

    AcademicYear domain = mapper.toDomain(entity);
    AcademicYearEntity result = mapper.toEntity(domain);

    assertEquals(id, domain.getId());
    assertEquals("2025-2026", domain.getLabel());
    assertEquals(id, result.getId());
  }

  @Test
  void shouldMapCourse() {
    UUID id = UUID.randomUUID();

    CourseMapper mapper = new CourseMapper();

    CourseEntity entity =
        CourseEntity.builder().id(id).ref("PROG4").title("Programming").credits(6).build();

    Course domain = mapper.toDomain(entity);
    CourseEntity result = mapper.toEntity(domain);

    assertEquals("PROG4", domain.getRef());
    assertEquals(6, domain.getCredits());
    assertEquals(id, result.getId());
  }

  @Test
  void shouldMapCourseOffering() {
    UUID id = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    UUID yearId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();

    CourseOfferingMapper mapper = new CourseOfferingMapper();

    CourseOfferingEntity entity =
        CourseOfferingEntity.builder()
            .id(id)
            .course(CourseEntity.builder().id(courseId).build())
            .academicYear(AcademicYearEntity.builder().id(yearId).build())
            .group(GroupEntity.builder().id(groupId).build())
            .build();

    CourseOffering domain = mapper.toDomain(entity);
    CourseOfferingEntity result = mapper.toEntity(domain);

    assertEquals(courseId, domain.getCourseId());
    assertEquals(yearId, domain.getAcademicYearId());
    assertEquals(groupId, result.getGroup().getId());
  }

  @Test
  void shouldMapCourseOfferingTeacher() {
    UUID id = UUID.randomUUID();
    UUID offeringId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();

    CourseOfferingTeacherMapper mapper = new CourseOfferingTeacherMapper();

    CourseOfferingTeacherEntity entity =
        CourseOfferingTeacherEntity.builder()
            .id(id)
            .courseOffering(CourseOfferingEntity.builder().id(offeringId).build())
            .teacher(UserEntity.builder().id(teacherId).build())
            .build();

    CourseOfferingTeacher domain = mapper.toDomain(entity);
    CourseOfferingTeacherEntity result = mapper.toEntity(domain);

    assertEquals(offeringId, domain.getCourseOfferingId());
    assertEquals(teacherId, domain.getTeacherId());
    assertEquals(teacherId, result.getTeacher().getId());
  }

  @Test
  void shouldMapExam() {
    UUID id = UUID.randomUUID();
    UUID offeringId = UUID.randomUUID();

    ExamMapper mapper = new ExamMapper();

    ExamEntity entity =
        ExamEntity.builder()
            .id(id)
            .ref("EXAM1")
            .courseOffering(CourseOfferingEntity.builder().id(offeringId).build())
            .coefficient(BigDecimal.valueOf(2))
            .build();

    Exam domain = mapper.toDomain(entity);
    ExamEntity result = mapper.toEntity(domain);

    assertEquals("EXAM1", domain.getRef());
    assertEquals(offeringId, domain.getCourseOfferingId());
    assertEquals(id, result.getId());
  }

  @Test
  void shouldMapGrade() {
    UUID id = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID enrollmentId = UUID.randomUUID();
    Instant now = Instant.now();

    GradeMapper mapper = new GradeMapper();

    GradeEntity entity =
        GradeEntity.builder()
            .id(id)
            .exam(ExamEntity.builder().id(examId).build())
            .studentCourseEnrollment(
                StudentCourseEnrollmentEntity.builder().id(enrollmentId).build())
            .score(BigDecimal.valueOf(14))
            .updatedAt(now)
            .build();

    Grade domain = mapper.toDomain(entity);
    GradeEntity result = mapper.toEntity(domain);

    assertEquals(examId, domain.getExamId());
    assertEquals(enrollmentId, domain.getStudentCourseEnrollmentId());
    assertEquals(BigDecimal.valueOf(14), result.getScore());
  }

  @Test
  void shouldMapGradeHistory() {
    UUID id = UUID.randomUUID();
    UUID gradeId = UUID.randomUUID();
    UUID changedBy = UUID.randomUUID();
    Instant now = Instant.now();

    GradeHistoryMapper mapper = new GradeHistoryMapper();

    GradeHistoryEntity entity =
        GradeHistoryEntity.builder()
            .id(id)
            .grade(GradeEntity.builder().id(gradeId).build())
            .oldScore(BigDecimal.valueOf(10))
            .newScore(BigDecimal.valueOf(15))
            .reason("Correction")
            .changedBy(UserEntity.builder().id(changedBy).build())
            .changedAt(now)
            .build();

    GradeHistory domain = mapper.toDomain(entity);
    GradeHistoryEntity result = mapper.toEntity(domain);

    assertEquals(gradeId, domain.getGradeId());
    assertEquals("Correction", domain.getReason());
    assertEquals(changedBy, result.getChangedBy().getId());
  }

  @Test
  void shouldMapGroup() {
    UUID id = UUID.randomUUID();

    GroupMapper mapper = new GroupMapper();

    GroupEntity entity = GroupEntity.builder().id(id).ref("K1").build();

    Group domain = mapper.toDomain(entity);
    GroupEntity result = mapper.toEntity(domain);

    assertEquals("K1", domain.getRef());
    assertEquals(id, result.getId());
  }

  @Test
  void shouldMapPromotion() {
    UUID id = UUID.randomUUID();

    PromotionMapper mapper = new PromotionMapper();

    PromotionEntity entity =
        PromotionEntity.builder()
            .id(id)
            .name("Promotion 2025")
            .startYear(2025)
            .endYear(2028)
            .build();

    Promotion domain = mapper.toDomain(entity);
    PromotionEntity result = mapper.toEntity(domain);

    assertEquals("Promotion 2025", domain.getName());
    assertEquals(2025, result.getStartYear());
  }

  @Test
  void shouldMapStudentCourseEnrollment() {
    UUID id = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    UUID yearId = UUID.randomUUID();
    Instant now = Instant.now();

    StudentCourseEnrollmentMapper mapper = new StudentCourseEnrollmentMapper();

    StudentCourseEnrollmentEntity entity =
        StudentCourseEnrollmentEntity.builder()
            .id(id)
            .student(UserEntity.builder().id(studentId).build())
            .course(CourseEntity.builder().id(courseId).build())
            .academicYear(AcademicYearEntity.builder().id(yearId).build())
            .enrolledAt(now)
            .build();

    StudentCourseEnrollment domain = mapper.toDomain(entity);
    StudentCourseEnrollmentEntity result = mapper.toEntity(domain);

    assertEquals(studentId, domain.getStudentId());
    assertEquals(courseId, domain.getCourseId());
    assertEquals(yearId, result.getAcademicYear().getId());
  }

  @Test
  void shouldMapStudentGroupHistory() {
    UUID id = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    UUID yearId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    Instant now = Instant.now();

    StudentGroupHistoryMapper mapper = new StudentGroupHistoryMapper();

    StudentGroupHistoryEntity entity =
        StudentGroupHistoryEntity.builder()
            .id(id)
            .student(UserEntity.builder().id(studentId).build())
            .academicYear(AcademicYearEntity.builder().id(yearId).build())
            .group(GroupEntity.builder().id(groupId).build())
            .pathway(PathwayEntity.EL)
            .startedAt(now)
            .build();

    StudentGroupHistory domain = mapper.toDomain(entity);
    StudentGroupHistoryEntity result = mapper.toEntity(domain);

    assertEquals(Pathway.EL, domain.getPathway());
    assertEquals(groupId, domain.getGroupId());
    assertEquals(PathwayEntity.EL, result.getPathway());
  }

  @Test
  void shouldMapUser() {
    UUID id = UUID.randomUUID();
    UUID promotionId = UUID.randomUUID();

    UserMapper mapper = new UserMapper();

    UserEntity entity =
        UserEntity.builder()
            .id(id)
            .firstName("Jane")
            .lastName("Doe")
            .email("jane@hei.school")
            .passwordHash("hashed")
            .role(UserRoleEntity.STUDENT)
            .status(UserStatusEntity.ACTIVE)
            .std("STD25001")
            .promotion(PromotionEntity.builder().id(promotionId).build())
            .build();

    User domain = mapper.toDomain(entity);
    UserEntity result = mapper.toEntity(domain);

    assertEquals(UserRole.STUDENT, domain.getRole());
    assertEquals(UserStatus.ACTIVE, domain.getStatus());
    assertEquals(promotionId, domain.getPromotionId());
    assertEquals(promotionId, result.getPromotion().getId());
  }

  @Test
  void shouldMapNullableStudentFields() {
    UserMapper userMapper = new UserMapper();

    UserEntity entity =
        UserEntity.builder()
            .id(UUID.randomUUID())
            .firstName("Admin")
            .lastName("HEI")
            .email("admin@hei.school")
            .passwordHash("hashed")
            .role(UserRoleEntity.ADMIN)
            .status(UserStatusEntity.ACTIVE)
            .build();

    User domain = userMapper.toDomain(entity);
    UserEntity result = userMapper.toEntity(domain);

    assertNull(domain.getPromotionId());
    assertNull(result.getPromotion());

    StudentGroupHistoryMapper historyMapper = new StudentGroupHistoryMapper();

    StudentGroupHistory history =
        StudentGroupHistory.builder()
            .id(UUID.randomUUID())
            .studentId(UUID.randomUUID())
            .academicYearId(UUID.randomUUID())
            .groupId(UUID.randomUUID())
            .pathway(null)
            .startedAt(Instant.now())
            .build();

    assertNull(historyMapper.toEntity(history).getPathway());
  }
}
