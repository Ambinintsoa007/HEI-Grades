package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.endpoint.rest.dto.GraduateResponse;
import com.example.demo.endpoint.rest.dto.PromotionResultsResponse;
import com.example.demo.endpoint.rest.dto.PromotionStudentResultResponse;
import com.example.demo.endpoint.rest.dto.StudentAcademicYearResultResponse;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.model.Pathway;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.PromotionEntity;
import com.example.demo.repository.model.UserEntity;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraduateExcelServiceTest {

  private PromotionResultService promotionResultService;
  private PromotionRepository promotionRepository;
  private UserRepository userRepository;
  private GraduateExcelService graduateExcelService;

  private UUID promotionId;

  @BeforeEach
  void setUp() {
    promotionResultService = mock(PromotionResultService.class);
    promotionRepository = mock(PromotionRepository.class);
    userRepository = mock(UserRepository.class);
    graduateExcelService =
        new GraduateExcelService(promotionResultService, promotionRepository, userRepository);

    promotionId = UUID.randomUUID();
    when(promotionRepository.findById(promotionId))
        .thenReturn(
            Optional.of(PromotionEntity.builder().id(promotionId).name("Promo 2027").build()));
    when(userRepository.findAllById(anyList())).thenReturn(List.of());
  }

  @Test
  void generatedBytesAreReadableAsXSSFWorkbook() throws Exception {
    stubGraduate("STD-1", 6);

    byte[] bytes = graduateExcelService.generate(promotionId);

    try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      Sheet sheet = workbook.getSheet("Graduates");
      assertNotNull(sheet);
      assertEquals("Graduates", sheet.getSheetName());
    }
  }

  @Test
  void workbookContainsExpectedHeaders() throws Exception {
    when(promotionResultService.getGraduates(promotionId)).thenReturn(List.of());
    when(promotionResultService.getPromotionResults(promotionId))
        .thenReturn(
            PromotionResultsResponse.builder()
                .promotionId(promotionId)
                .students(List.of())
                .build());

    byte[] bytes = graduateExcelService.generate(promotionId);

    try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      Sheet sheet = workbook.getSheet("Graduates");
      var header = sheet.getRow(0);
      assertEquals("STD", header.getCell(0).getStringCellValue());
      assertEquals("Last name", header.getCell(1).getStringCellValue());
      assertEquals("First name", header.getCell(2).getStringCellValue());
      assertEquals("Email", header.getCell(3).getStringCellValue());
      assertEquals("Promotion", header.getCell(4).getStringCellValue());
      assertEquals("Earned credits", header.getCell(5).getStringCellValue());
    }
  }

  @Test
  void workbookContainsOnlyGraduatesWithEmailAndEarnedCredits() throws Exception {
    var studentA = UUID.randomUUID();
    var studentB = UUID.randomUUID();
    when(promotionResultService.getGraduates(promotionId))
        .thenReturn(List.of(graduate("STD-10", studentA), graduate("STD-11", studentB)));
    when(promotionResultService.getPromotionResults(promotionId))
        .thenReturn(
            PromotionResultsResponse.builder()
                .promotionId(promotionId)
                .students(
                    List.of(
                        studentResult(studentA, "STD-10", 6),
                        studentResult(studentB, "STD-11", 12)))
                .build());
    when(userRepository.findAllById(anyList()))
        .thenReturn(List.of(user(studentA, "jane@hei.school"), user(studentB, "jane@hei.school")));

    byte[] bytes = graduateExcelService.generate(promotionId);

    try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      Sheet sheet = workbook.getSheet("Graduates");
      assertEquals(3, sheet.getPhysicalNumberOfRows());
      assertEquals("STD-10", sheet.getRow(1).getCell(0).getStringCellValue());
      assertEquals("STD-11", sheet.getRow(2).getCell(0).getStringCellValue());
      assertEquals("Promo 2027", sheet.getRow(1).getCell(4).getStringCellValue());
      assertEquals("jane@hei.school", sheet.getRow(1).getCell(3).getStringCellValue());
      assertEquals(6, sheet.getRow(1).getCell(5).getNumericCellValue());
      assertEquals(12, sheet.getRow(2).getCell(5).getNumericCellValue());
    }
    verify(promotionResultService).getGraduates(promotionId);
  }

  @Test
  void missingPromotionThrowsResourceNotFoundException() {
    UUID missingId = UUID.randomUUID();
    when(promotionRepository.findById(missingId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> graduateExcelService.generate(missingId));
  }

  @Test
  void filenameUsesPromotionName() {
    assertEquals("graduates-Promo 2027.xlsx", graduateExcelService.filename(promotionId));
  }

  private void stubGraduate(String std, int earnedCredits) {
    stubGraduate(std, UUID.randomUUID(), earnedCredits);
  }

  private void stubGraduate(String std, UUID studentId, int earnedCredits) {
    when(promotionResultService.getGraduates(promotionId))
        .thenReturn(List.of(graduate(std, studentId)));
    when(promotionResultService.getPromotionResults(promotionId))
        .thenReturn(
            PromotionResultsResponse.builder()
                .promotionId(promotionId)
                .students(List.of(studentResult(studentId, std, earnedCredits)))
                .build());
    when(userRepository.findAllById(anyList()))
        .thenReturn(List.of(user(studentId, "jane@hei.school")));
  }

  private GraduateResponse graduate(String std, UUID studentId) {
    return GraduateResponse.builder()
        .studentId(studentId)
        .std(std)
        .firstName("Jane")
        .lastName("Doe")
        .pathway(Pathway.EL)
        .overallAverage(new BigDecimal("13.00"))
        .build();
  }

  private PromotionStudentResultResponse studentResult(
      UUID studentId, String std, int earnedCredits) {
    return PromotionStudentResultResponse.builder()
        .studentId(studentId)
        .std(std)
        .firstName("Jane")
        .lastName("Doe")
        .complete(true)
        .graduate(true)
        .academicYears(
            List.of(
                StudentAcademicYearResultResponse.builder()
                    .academicYearId(UUID.randomUUID())
                    .academicYearLabel("2023-2024")
                    .average(new BigDecimal("13.00"))
                    .earnedCredits(earnedCredits)
                    .complete(true)
                    .build()))
        .build();
  }

  private UserEntity user(UUID id, String email) {
    return UserEntity.builder().id(id).email(email).build();
  }
}
