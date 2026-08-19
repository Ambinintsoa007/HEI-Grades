package com.example.demo.repository;

import com.example.demo.repository.model.GradeEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<GradeEntity, UUID> {

  List<GradeEntity> findByStudentCourseEnrollment_Id(UUID enrollmentId);

  Optional<GradeEntity> findByExam_IdAndStudentCourseEnrollment_Id(UUID examId, UUID enrollmentId);

  List<GradeEntity> findByExam_Id(UUID examId);

  List<GradeEntity> findByStudentCourseEnrollment_Student_Id(UUID studentId);
}
