package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.endpoint.rest.dto.CreateAcademicYearRequest;
import com.example.demo.endpoint.rest.dto.UpdateAcademicYearRequest;
import com.example.demo.endpoint.rest.exception.ApiException;
import com.example.demo.mapper.AcademicYearMapper;
import com.example.demo.model.AcademicYear;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.model.AcademicYearEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AcademicYearServiceTest {

  private AcademicYearRepository academicYearRepository;
  private AcademicYearMapper academicYearMapper;
  private AcademicYearService academicYearService;

  @BeforeEach
  void setUp() {
    academicYearRepository = mock(AcademicYearRepository.class);
    academicYearMapper = mock(AcademicYearMapper.class);

    academicYearService = new AcademicYearService(academicYearRepository, academicYearMapper);
  }

  @Test
  void shouldGetAllAcademicYears() {
    AcademicYearEntity entity = new AcademicYearEntity();

    AcademicYear year =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 7, 31))
            .build();

    when(academicYearRepository.findAll()).thenReturn(List.of(entity));
    when(academicYearMapper.toDomain(entity)).thenReturn(year);

    var result = academicYearService.getAll();

    assertEquals(1, result.size());
    assertEquals("2025-2026", result.getFirst().getLabel());
  }

  @Test
  void shouldCreateAcademicYear() {
    CreateAcademicYearRequest request = new CreateAcademicYearRequest();
    request.setLabel("2025-2026");
    request.setStartDate(LocalDate.of(2025, 9, 1));
    request.setEndDate(LocalDate.of(2026, 7, 31));

    AcademicYearEntity entity = new AcademicYearEntity();

    AcademicYear saved =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label("2025-2026")
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .build();

    when(academicYearRepository.existsByLabelIgnoreCase("2025-2026")).thenReturn(false);
    when(academicYearMapper.toEntity(any(AcademicYear.class))).thenReturn(entity);
    when(academicYearRepository.save(entity)).thenReturn(entity);
    when(academicYearMapper.toDomain(entity)).thenReturn(saved);

    var result = academicYearService.create(request);

    assertEquals("2025-2026", result.getLabel());
  }

  @Test
  void shouldRejectDuplicateAcademicYear() {
    CreateAcademicYearRequest request = validRequest();

    when(academicYearRepository.existsByLabelIgnoreCase(request.getLabel())).thenReturn(true);

    ApiException exception =
        assertThrows(ApiException.class, () -> academicYearService.create(request));

    assertEquals(HttpStatus.CONFLICT, exception.getStatus());
  }

  @Test
  void shouldRejectInvalidDates() {
    CreateAcademicYearRequest request = validRequest();
    request.setStartDate(LocalDate.of(2026, 9, 1));
    request.setEndDate(LocalDate.of(2026, 7, 1));

    ApiException exception =
        assertThrows(ApiException.class, () -> academicYearService.create(request));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
  }

  @Test
  void shouldUpdateAcademicYear() {
    UUID id = UUID.randomUUID();
    AcademicYearEntity entity = new AcademicYearEntity();

    AcademicYear current =
        AcademicYear.builder()
            .id(id)
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 7, 31))
            .build();

    AcademicYear updated =
        AcademicYear.builder()
            .id(id)
            .label("2025/2026")
            .startDate(current.getStartDate())
            .endDate(current.getEndDate())
            .build();

    UpdateAcademicYearRequest request = new UpdateAcademicYearRequest();
    request.setLabel("2025/2026");

    when(academicYearRepository.findById(id)).thenReturn(Optional.of(entity));
    when(academicYearMapper.toDomain(entity)).thenReturn(current, updated);
    when(academicYearMapper.toEntity(any(AcademicYear.class))).thenReturn(entity);
    when(academicYearRepository.save(entity)).thenReturn(entity);

    var result = academicYearService.update(id, request);

    assertEquals("2025/2026", result.getLabel());
  }

  @Test
  void shouldRejectUnknownAcademicYearOnUpdate() {
    UUID id = UUID.randomUUID();

    when(academicYearRepository.findById(id)).thenReturn(Optional.empty());

    ApiException exception =
        assertThrows(
            ApiException.class,
            () -> academicYearService.update(id, new UpdateAcademicYearRequest()));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
  }

  private CreateAcademicYearRequest validRequest() {
    CreateAcademicYearRequest request = new CreateAcademicYearRequest();
    request.setLabel("2025-2026");
    request.setStartDate(LocalDate.of(2025, 9, 1));
    request.setEndDate(LocalDate.of(2026, 7, 31));
    return request;
  }
}
