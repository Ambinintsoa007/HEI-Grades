package com.example.demo.repository;

import com.example.demo.repository.model.CourseOfferingTeacherEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseOfferingTeacherRepository
    extends JpaRepository<CourseOfferingTeacherEntity, UUID> {

  List<CourseOfferingTeacherEntity> findByTeacher_Id(UUID teacherId);

  List<CourseOfferingTeacherEntity> findByCourseOffering_Id(UUID courseOfferingId);

  boolean existsByCourseOffering_IdAndTeacher_Id(UUID courseOfferingId, UUID teacherId);
}
