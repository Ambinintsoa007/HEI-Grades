package com.example.demo.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.CourseManagementTestBase;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeRepository;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.StudentCourseEnrollmentRepository;
import com.example.demo.repository.model.CourseOfferingEntity;
import com.example.demo.repository.model.ExamEntity;
import com.example.demo.repository.model.GradeEntity;
import com.example.demo.repository.model.PromotionEntity;
import com.example.demo.repository.model.StudentCourseEnrollmentEntity;
import com.example.demo.repository.model.UserEntity;
import com.example.demo.repository.model.UserRoleEntity;
import com.example.demo.repository.model.UserStatusEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class PromotionResultControllerIT extends CourseManagementTestBase {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private PromotionRepository promotionRepository;
  @Autowired private CourseOfferingRepository courseOfferingRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private GradeRepository gradeRepository;
  @Autowired private StudentCourseEnrollmentRepository studentCourseEnrollmentRepository;

  private PromotionEntity promotion;
  private UserEntity graduateStudent;
  private UserEntity incompleteStudent;
  private UserEntity failingStudent;
  private String resultsUrl;
  private String graduatesUrl;
  private String xlsxUrl;

  @BeforeEach
  void setUp() {
    promotion =
        promotionRepository.save(
            PromotionEntity.builder()
                .id(UUID.randomUUID())
                .name("Promo 2027 " + UUID.randomUUID().toString().substring(0, 8))
                .startYear(2024)
                .endYear(2027)
                .build());
    graduateStudent = saveStudentInPromotion(promotion);
    incompleteStudent = saveStudentInPromotion(promotion);
    failingStudent = saveStudentInPromotion(promotion);

    var setup1 = courseSetup("2023-2024");
    var setup2 = courseSetup("2024-2025");
    var setup3 = courseSetup("2025-2026");

    saveEnrollment(graduateStudent, setup1);
    saveEnrollment(graduateStudent, setup2);
    saveEnrollment(graduateStudent, setup3);
    saveEnrollment(incompleteStudent, setup1);
    saveEnrollment(incompleteStudent, setup2);
    saveEnrollment(incompleteStudent, setup3);
    saveEnrollment(failingStudent, setup1);
    saveEnrollment(failingStudent, setup2);
    saveEnrollment(failingStudent, setup3);

    saveGrade(graduateStudent, setup1, new BigDecimal("12.00"));
    saveGrade(graduateStudent, setup2, new BigDecimal("13.00"));
    saveGrade(graduateStudent, setup3, new BigDecimal("14.00"));

    saveGrade(failingStudent, setup1, new BigDecimal("12.00"));
    saveGrade(failingStudent, setup2, new BigDecimal("13.00"));
    saveGrade(failingStudent, setup3, new BigDecimal("8.00"));

    saveGrade(incompleteStudent, setup1, new BigDecimal("12.00"));
    saveGrade(incompleteStudent, setup2, new BigDecimal("13.00"));

    resultsUrl = "/promotions/" + promotion.getId() + "/results";
    graduatesUrl = "/promotions/" + promotion.getId() + "/graduates";
    xlsxUrl = "/promotions/" + promotion.getId() + "/graduates.xlsx";
  }

  @Test
  void adminCanGetPromotionResults() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            resultsUrl, HttpMethod.GET, new HttpEntity<>(authHeaders(saveAdmin())), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains(promotion.getId().toString()));
    assertTrue(response.getBody().contains(graduateStudent.getId().toString()));
    assertTrue(response.getBody().contains(incompleteStudent.getId().toString()));
    assertTrue(response.getBody().contains(failingStudent.getId().toString()));
  }

  @Test
  void adminGetsOnlyGraduates() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            graduatesUrl, HttpMethod.GET, new HttpEntity<>(authHeaders(saveAdmin())), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("overallAverage"));
    assertTrue(response.getBody().contains(graduateStudent.getId().toString()));
    assertTrue(!response.getBody().contains(incompleteStudent.getId().toString()));
    assertTrue(!response.getBody().contains(failingStudent.getId().toString()));
  }

  @Test
  void adminCanDownloadXlsxWithCorrectHeaders() {
    ResponseEntity<byte[]> response =
        restTemplate.exchange(
            xlsxUrl, HttpMethod.GET, new HttpEntity<>(authHeaders(saveAdmin())), byte[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(
        response
            .getHeaders()
            .getContentType()
            .toString()
            .startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    assertTrue(
        response
            .getHeaders()
            .getFirst(HttpHeaders.CONTENT_DISPOSITION)
            .contains("attachment; filename=\"graduates-Promo 2027 "));
    assertTrue(response.getBody() != null && response.getBody().length > 0);
  }

  @Test
  void xlsxContentDispositionUsesUtf8EncodedFilename() {
    var accentedPromotion =
        promotionRepository.save(
            PromotionEntity.builder()
                .id(UUID.randomUUID())
                .name("TEST PROMO été " + UUID.randomUUID().toString().substring(0, 8))
                .startYear(2024)
                .endYear(2027)
                .build());

    ResponseEntity<byte[]> response =
        restTemplate.exchange(
            "/promotions/" + accentedPromotion.getId() + "/graduates.xlsx",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(saveAdmin())),
            byte[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    String disposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
    assertTrue(disposition.contains("filename*=UTF-8''"), disposition);
    assertTrue(disposition.contains("%C3%A9"), disposition);
    assertTrue(!disposition.contains("été"), disposition);
  }

  @Test
  void teacherGets403OnAllEndpoints() {
    HttpHeaders headers = authHeaders(saveTeacher());

    assert403(resultsUrl, headers);
    assert403(graduatesUrl, headers);
    assert403(xlsxUrl, headers);
  }

  @Test
  void studentGets403OnAllEndpoints() {
    HttpHeaders headers = authHeaders(saveStudent());

    assert403(resultsUrl, headers);
    assert403(graduatesUrl, headers);
    assert403(xlsxUrl, headers);
  }

  @Test
  void unknownPromotionReturns404() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/promotions/" + UUID.randomUUID() + "/results",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(saveAdmin())),
            String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void anonymousReturns401() {
    ResponseEntity<String> response =
        restTemplate.exchange(resultsUrl, HttpMethod.GET, HttpEntity.EMPTY, String.class);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  private void assert403(String url, HttpHeaders headers) {
    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
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
    var course = saveCourse("CRS-RES-" + UUID.randomUUID().toString().substring(0, 8), 5);
    var year = saveAcademicYear(yearLabel + "-" + UUID.randomUUID().toString().substring(0, 8));
    var group = saveGroup("G-RES-" + UUID.randomUUID().toString().substring(0, 8));
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
                .ref("EXAM-RES-" + UUID.randomUUID().toString().substring(0, 8))
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

  private void saveGrade(UserEntity student, CourseSetup setup, BigDecimal score) {
    gradeRepository.save(
        GradeEntity.builder()
            .id(UUID.randomUUID())
            .exam(setup.exam())
            .studentCourseEnrollment(
                studentCourseEnrollmentRepository
                    .findByStudent_IdAndCourse_IdAndAcademicYear_Id(
                        student.getId(), setup.course().getId(), setup.year().getId())
                    .orElseThrow())
            .score(score)
            .updatedAt(Instant.now())
            .build());
  }
}
