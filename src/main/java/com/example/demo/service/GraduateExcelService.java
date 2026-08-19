package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.GraduateResponse;
import com.example.demo.endpoint.rest.dto.PromotionStudentResultResponse;
import com.example.demo.endpoint.rest.dto.StudentAcademicYearResultResponse;
import com.example.demo.endpoint.rest.exception.ResourceNotFoundException;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.UserEntity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraduateExcelService {

  private static final String SHEET_NAME = "Graduates";
  private static final String[] HEADERS = {
    "STD", "Last name", "First name", "Email", "Promotion", "Earned credits"
  };

  private final PromotionResultService promotionResultService;
  private final PromotionRepository promotionRepository;
  private final UserRepository userRepository;

  public byte[] generate(UUID promotionId) {
    var promotion =
        promotionRepository
            .findById(promotionId)
            .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
    var graduates = promotionResultService.getGraduates(promotionId);
    var earnedCreditsByStudent =
        promotionResultService.getPromotionResults(promotionId).getStudents().stream()
            .filter(PromotionStudentResultResponse::isGraduate)
            .collect(
                Collectors.toMap(
                    PromotionStudentResultResponse::getStudentId,
                    student ->
                        student.getAcademicYears().stream()
                            .mapToInt(StudentAcademicYearResultResponse::getEarnedCredits)
                            .sum()));
    var emailsByStudent =
        userRepository
            .findAllById(graduates.stream().map(GraduateResponse::getStudentId).toList())
            .stream()
            .collect(Collectors.toMap(UserEntity::getId, UserEntity::getEmail));

    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet(SHEET_NAME);

      Row header = sheet.createRow(0);
      for (int i = 0; i < HEADERS.length; i++) {
        header.createCell(i).setCellValue(HEADERS[i]);
      }

      int rowIndex = 1;
      for (var graduate : graduates) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(graduate.getStd());
        row.createCell(1).setCellValue(graduate.getLastName());
        row.createCell(2).setCellValue(graduate.getFirstName());
        row.createCell(3).setCellValue(emailsByStudent.get(graduate.getStudentId()));
        row.createCell(4).setCellValue(promotion.getName());
        row.createCell(5).setCellValue(earnedCreditsByStudent.get(graduate.getStudentId()));
      }

      for (int i = 0; i < HEADERS.length; i++) {
        sheet.autoSizeColumn(i);
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      workbook.write(out);
      return out.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate graduates Excel file", e);
    }
  }

  public String filename(UUID promotionId) {
    var promotion =
        promotionRepository
            .findById(promotionId)
            .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
    return "graduates-" + promotion.getName() + ".xlsx";
  }
}
