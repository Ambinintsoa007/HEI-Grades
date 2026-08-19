package com.example.demo.service;

import com.example.demo.endpoint.rest.dto.TranscriptResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

@Service
public class TranscriptPdfService {

  public byte[] generate(TranscriptResponse transcript) {
    try (var document = new PDDocument();
        var output = new ByteArrayOutputStream()) {

      var page = new PDPage(PDRectangle.A4);
      document.addPage(page);

      var regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

      var boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

      try (var content = new PDPageContentStream(document, page)) {

        float y = 790;

        y = writeText(content, boldFont, 18, 50, y, "HEI - Student Transcript");
        y -= 15;

        y = writeText(content, regularFont, 11, 50, y, "Student ID: " + transcript.getStudentId());

        y =
            writeText(
                content,
                regularFont,
                11,
                50,
                y,
                "Status: " + (transcript.isComplete() ? "Complete" : "Incomplete"));

        y -= 15;

        y = writeText(content, boldFont, 11, 50, y, "Course | Credits | Final grade | Validated");

        for (var course : transcript.getCourses()) {

          String finalGrade =
              course.getFinalGrade() == null ? "N/A" : course.getFinalGrade().toPlainString();

          String validated =
              course.getValidated() == null ? "N/A" : course.getValidated() ? "Yes" : "No";

          String line =
              course.getRef()
                  + " - "
                  + course.getTitle()
                  + " | "
                  + course.getCredits()
                  + " | "
                  + finalGrade
                  + " | "
                  + validated;

          y = writeText(content, regularFont, 9, 50, y, line);
        }

        y -= 15;

        String average =
            transcript.getAnnualAverage() == null
                ? "N/A"
                : transcript.getAnnualAverage().toPlainString();

        y = writeText(content, regularFont, 11, 50, y, "Annual average: " + average);

        writeText(
            content, regularFont, 11, 50, y, "Earned credits: " + transcript.getEarnedCredits());
      }

      document.save(output);
      return output.toByteArray();

    } catch (IOException e) {
      throw new IllegalStateException("Failed to generate transcript PDF", e);
    }
  }

  private float writeText(
      PDPageContentStream content, PDType1Font font, float fontSize, float x, float y, String text)
      throws IOException {

    content.beginText();
    content.setFont(font, fontSize);
    content.newLineAtOffset(x, y);
    content.showText(text);
    content.endText();

    return y - 18;
  }
}
