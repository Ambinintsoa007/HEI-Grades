package com.example.demo.repository;

import com.example.demo.repository.model.StudentCourseEnrollmentEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentCourseEnrollmentRepository
    extends JpaRepository<StudentCourseEnrollmentEntity, UUID> {

  @EntityGraph(attributePaths = "courseOfferings")
  List<StudentCourseEnrollmentEntity> findAllByStudent_Id(UUID studentId);

  List<StudentCourseEnrollmentEntity> findByStudent_IdAndAcademicYear_Id(
      UUID studentId, UUID academicYearId);

  List<StudentCourseEnrollmentEntity> findByStudent_Id(UUID studentId);

  boolean existsByStudent_IdAndCourse_IdAndAcademicYear_Id(
      UUID studentId, UUID courseId, UUID academicYearId);

  Optional<StudentCourseEnrollmentEntity> findByStudent_IdAndCourse_IdAndAcademicYear_Id(
      UUID studentId, UUID courseId, UUID academicYearId);

  List<StudentCourseEnrollmentEntity> findByCourseOfferings_Id(UUID courseOfferingId);
}
