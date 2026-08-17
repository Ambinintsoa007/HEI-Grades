package com.example.demo;

import com.example.demo.conf.FacadeIT;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.AcademicYearEntity;
import com.example.demo.repository.model.CourseEntity;
import com.example.demo.repository.model.GroupEntity;
import com.example.demo.repository.model.PromotionEntity;
import com.example.demo.repository.model.UserEntity;
import com.example.demo.repository.model.UserRoleEntity;
import com.example.demo.repository.model.UserStatusEntity;
import com.example.demo.service.JwtService;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public abstract class CourseManagementTestBase extends FacadeIT {

  @Autowired protected CourseRepository courseRepository;
  @Autowired protected AcademicYearRepository academicYearRepository;
  @Autowired protected GroupRepository groupRepository;
  @Autowired protected PromotionRepository promotionRepository;
  @Autowired protected UserRepository userRepository;
  @Autowired protected JwtService jwtService;

  protected UserEntity saveAdmin() {
    return saveUser("admin-" + UUID.randomUUID() + "@test.com", UserRoleEntity.ADMIN);
  }

  protected UserEntity saveTeacher() {
    return saveUser("teacher-" + UUID.randomUUID() + "@test.com", UserRoleEntity.TEACHER);
  }

  protected UserEntity saveStudent() {
    return saveUser("student-" + UUID.randomUUID() + "@test.com", UserRoleEntity.STUDENT);
  }

  protected HttpHeaders authHeaders(UserEntity user) {
    var headers = new HttpHeaders();
    headers.setBearerAuth(jwtService.generateToken(user.getId(), user.getRole().name()));
    return headers;
  }

  protected CourseEntity saveCourse(String ref, int credits) {
    return courseRepository.save(
        CourseEntity.builder()
            .id(UUID.randomUUID())
            .ref(ref)
            .title("Course " + ref)
            .credits(credits)
            .build());
  }

  protected AcademicYearEntity saveAcademicYear(String label) {
    return academicYearRepository.save(
        AcademicYearEntity.builder()
            .id(UUID.randomUUID())
            .label(label)
            .startDate(LocalDate.of(2024, 10, 1))
            .endDate(LocalDate.of(2025, 9, 30))
            .build());
  }

  protected GroupEntity saveGroup(String ref) {
    return groupRepository.save(GroupEntity.builder().id(UUID.randomUUID()).ref(ref).build());
  }

  protected UserEntity saveUser(String email, UserRoleEntity role) {
    var builder =
        UserEntity.builder()
            .id(UUID.randomUUID())
            .firstName("First")
            .lastName("Last")
            .email(email)
            .passwordHash("password-hash")
            .role(role)
            .status(UserStatusEntity.ACTIVE);
    if (role == UserRoleEntity.STUDENT) {
      PromotionEntity promotion =
          promotionRepository.save(
              PromotionEntity.builder()
                  .id(UUID.randomUUID())
                  .name("Promotion " + email)
                  .startYear(2024)
                  .endYear(2027)
                  .build());
      builder.std("STD-" + UUID.randomUUID().toString().substring(0, 8)).promotion(promotion);
    }
    return userRepository.save(builder.build());
  }
}
