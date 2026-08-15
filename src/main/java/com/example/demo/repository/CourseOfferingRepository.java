package com.example.demo.repository;

import com.example.demo.repository.model.CourseOfferingEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseOfferingRepository extends JpaRepository<CourseOfferingEntity, UUID> {

  Optional<CourseOfferingEntity> findByCourse_IdAndAcademicYear_IdAndGroup_Id(
      UUID courseId, UUID academicYearId, UUID groupId);

  List<CourseOfferingEntity> findByAcademicYear_IdAndGroup_Id(UUID academicYearId, UUID groupId);

  List<CourseOfferingEntity> findByAcademicYear_Id(UUID academicYearId);

  List<CourseOfferingEntity> findByGroup_Id(UUID groupId);
}
