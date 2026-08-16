package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.AcademicYearResponse;
import com.example.demo.endpoint.rest.dto.CreateAcademicYearRequest;
import com.example.demo.endpoint.rest.dto.UpdateAcademicYearRequest;
import com.example.demo.service.AcademicYearService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/academic-years")
@RequiredArgsConstructor
public class AcademicYearController {

  private final AcademicYearService academicYearService;

  @GetMapping
  public ResponseEntity<List<AcademicYearResponse>> getAcademicYears() {
    return ResponseEntity.ok(academicYearService.getAll());
  }

  @PostMapping
  public ResponseEntity<AcademicYearResponse> createAcademicYear(
      @Valid @RequestBody CreateAcademicYearRequest request) {

    return ResponseEntity.status(HttpStatus.CREATED).body(academicYearService.create(request));
  }

  @PatchMapping("/{academicYearId}")
  public ResponseEntity<AcademicYearResponse> updateAcademicYear(
      @PathVariable UUID academicYearId, @RequestBody UpdateAcademicYearRequest request) {

    return ResponseEntity.ok(academicYearService.update(academicYearId, request));
  }
}
