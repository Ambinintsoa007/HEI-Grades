package com.example.demo.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.CourseManagementTestBase;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.StudentCourseEnrollmentRepository;
import com.example.demo.repository.model.CourseOfferingEntity;
import com.example.demo.repository.model.ExamEntity;
import com.example.demo.repository.model.PromotionEntity;
import com.example.demo.repository.model.StudentCourseEnrollmentEntity;
import com.example.demo.repository.model.UserEntity;
import com.example.demo.repository.model.UserRoleEntity;
import com.example.demo.repository.model.UserStatusEntity;
import com.example.demo.service.PromotionResultService;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PromotionResultQueryCountIT extends CourseManagementTestBase {

  private static final int STUDENTS = 12;
  private static final int YEARS = 3;
  private static final int COURSES_PER_YEAR = 3;

  @Autowired private PromotionResultService promotionResultService;
  @Autowired private PromotionRepository promotionRepository;
  @Autowired private CourseOfferingRepository courseOfferingRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private StudentCourseEnrollmentRepository studentCourseEnrollmentRepository;
  @Autowired private EntityManagerFactory entityManagerFactory;

  @Test
  void resultsQueryCountStaysLinearInNumberOfStudents() {
    var promotion =
        promotionRepository.save(
            PromotionEntity.builder()
                .id(UUID.randomUUID())
                .name("QueryCount " + UUID.randomUUID().toString().substring(0, 8))
                .startYear(2024)
                .endYear(2027)
                .build());

    for (int i = 0; i < STUDENTS; i++) {
      var student = saveStudentInPromotion(promotion);
      for (int year = 0; year < YEARS; year++) {
        for (int course = 0; course < COURSES_PER_YEAR; course++) {
          var setup = courseSetup(year + "-" + course);
          saveEnrollment(student, setup);
        }
      }
    }

    long queries =
        countQueries(() -> promotionResultService.getPromotionResults(promotion.getId()));

    assertTrue(queries < 400, "results query count too high (N+1): " + queries);
  }

  private long countQueries(Runnable call) {
    SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
    Statistics statistics = sessionFactory.getStatistics();
    statistics.setStatisticsEnabled(true);
    statistics.clear();
    call.run();
    return statistics.getPrepareStatementCount();
  }

  private UserEntity saveStudentInPromotion(PromotionEntity promotion) {
    return userRepository.save(
        UserEntity.builder()
            .id(UUID.randomUUID())
            .firstName("Jane")
            .lastName("Doe")
            .email("student-" + UUID.randomUUID() + "@test.com")
            .passwordHash("password-hash")
            .role(UserRoleEntity.STUDENT)
            .status(UserStatusEntity.ACTIVE)
            .std("STD-" + UUID.randomUUID().toString().substring(0, 8))
            .promotion(promotion)
            .build());
  }

  private record CourseSetup(
      com.example.demo.repository.model.CourseEntity course,
      com.example.demo.repository.model.AcademicYearEntity year,
      CourseOfferingEntity offering,
      ExamEntity exam) {}

  private CourseSetup courseSetup(String yearLabel) {
    var course = saveCourse("QC-" + UUID.randomUUID().toString().substring(0, 8), 5);
    var year = saveAcademicYear(yearLabel + "-" + UUID.randomUUID().toString().substring(0, 8));
    var group = saveGroup("G-QC-" + UUID.randomUUID().toString().substring(0, 8));
    var offering =
        courseOfferingRepository.save(
            CourseOfferingEntity.builder()
                .id(UUID.randomUUID())
                .course(course)
                .academicYear(year)
                .group(group)
                .build());
    var exam =
        examRepository.save(
            ExamEntity.builder()
                .id(UUID.randomUUID())
                .ref("EXAM-QC-" + UUID.randomUUID().toString().substring(0, 8))
                .courseOffering(offering)
                .coefficient(BigDecimal.ONE)
                .build());
    return new CourseSetup(course, year, offering, exam);
  }

  private void saveEnrollment(UserEntity student, CourseSetup setup) {
    studentCourseEnrollmentRepository.save(
        StudentCourseEnrollmentEntity.builder()
            .id(UUID.randomUUID())
            .student(student)
            .course(setup.course())
            .academicYear(setup.year())
            .enrolledAt(Instant.now())
            .courseOfferings(Set.of(setup.offering()))
            .build());
  }
}
