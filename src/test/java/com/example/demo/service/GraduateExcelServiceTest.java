package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.endpoint.rest.dto.GraduateResponse;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.model.PromotionEntity;
import java.io.ByteArrayInputStream;
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
  private GraduateExcelService graduateExcelService;

  private UUID promotionId;

  @BeforeEach
  void setUp() {
    promotionResultService = mock(PromotionResultService.class);
    promotionRepository = mock(PromotionRepository.class);
    graduateExcelService = new GraduateExcelService(promotionResultService, promotionRepository);

    promotionId = UUID.randomUUID();
    when(promotionRepository.findById(promotionId))
        .thenReturn(
            Optional.of(PromotionEntity.builder().id(promotionId).name("Promo 2027").build()));
  }

  @Test
  void generatedBytesAreReadableAsXSSFWorkbook() throws Exception {
    when(promotionResultService.getGraduates(promotionId)).thenReturn(List.of(graduate("STD-1")));

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
  void workbookContainsOnlyGraduates() throws Exception {
    var graduateA = graduate("STD-10");
    var graduateB = graduate("STD-11");
    when(promotionResultService.getGraduates(promotionId))
        .thenReturn(List.of(graduateA, graduateB));

    byte[] bytes = graduateExcelService.generate(promotionId);

    try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      Sheet sheet = workbook.getSheet("Graduates");
      assertEquals(3, sheet.getPhysicalNumberOfRows());
      assertEquals("STD-10", sheet.getRow(1).getCell(0).getStringCellValue());
      assertEquals("STD-11", sheet.getRow(2).getCell(0).getStringCellValue());
      assertEquals("Promo 2027", sheet.getRow(1).getCell(4).getStringCellValue());
      assertEquals(6, sheet.getRow(1).getCell(5).getNumericCellValue());
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

  private GraduateResponse graduate(String std) {
    return GraduateResponse.builder()
        .studentId(UUID.randomUUID())
        .std(std)
        .firstName("Jane")
        .lastName("Doe")
        .email("jane@hei.school")
        .earnedCredits(6)
        .build();
  }
}
