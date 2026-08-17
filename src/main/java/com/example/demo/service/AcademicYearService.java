package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.AcademicYearResponse;
import com.example.demo.endpoint.rest.dto.CreateAcademicYearRequest;
import com.example.demo.endpoint.rest.dto.UpdateAcademicYearRequest;
import com.example.demo.endpoint.rest.exception.BusinessException;
import com.example.demo.endpoint.rest.exception.ConflictException;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.mapper.AcademicYearMapper;
import com.example.demo.model.AcademicYear;
import com.example.demo.repository.AcademicYearRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AcademicYearService {

  private final AcademicYearRepository academicYearRepository;
  private final AcademicYearMapper academicYearMapper;

  public List<AcademicYearResponse> getAll() {
    return academicYearRepository.findAll().stream()
        .map(academicYearMapper::toDomain)
        .map(this::toResponse)
        .toList();
  }

  public AcademicYearResponse create(CreateAcademicYearRequest request) {
    validateDates(request.getStartDate(), request.getEndDate());

    if (academicYearRepository.existsByLabelIgnoreCase(request.getLabel())) {
      throw new ConflictException("Academic year already exists");
    }

    AcademicYear academicYear =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label(request.getLabel())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .build();

    return toResponse(
        academicYearMapper.toDomain(
            academicYearRepository.save(academicYearMapper.toEntity(academicYear))));
  }

  public AcademicYearResponse update(UUID academicYearId, UpdateAcademicYearRequest request) {

    AcademicYear current =
        academicYearRepository
            .findById(academicYearId)
            .map(academicYearMapper::toDomain)
            .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));

    String label = request.getLabel() != null ? request.getLabel() : current.getLabel();

    LocalDate startDate =
        request.getStartDate() != null ? request.getStartDate() : current.getStartDate();

    LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : current.getEndDate();

    validateDates(startDate, endDate);

    AcademicYear updated =
        AcademicYear.builder()
            .id(current.getId())
            .label(label)
            .startDate(startDate)
            .endDate(endDate)
            .build();

    return toResponse(
        academicYearMapper.toDomain(
            academicYearRepository.save(academicYearMapper.toEntity(updated))));
  }

  private void validateDates(LocalDate startDate, LocalDate endDate) {
    if (endDate.isBefore(startDate)) {
      throw new BusinessException("Academic year end date must be after start date");
    }
  }

  private AcademicYearResponse toResponse(AcademicYear academicYear) {
    return AcademicYearResponse.builder()
        .id(academicYear.getId())
        .label(academicYear.getLabel())
        .startDate(academicYear.getStartDate())
        .endDate(academicYear.getEndDate())
        .build();
  }
}
