package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.endpoint.rest.dto.TranscriptCourseResponse;
import com.example.demo.endpoint.rest.dto.TranscriptResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.apache.pdfbox.Loader;
import org.junit.jupiter.api.Test;

class TranscriptPdfServiceTest {

  private final TranscriptPdfService transcriptPdfService = new TranscriptPdfService();

  @Test
  void shouldGenerateValidPdf() throws Exception {
    var transcript =
        TranscriptResponse.builder()
            .studentId(UUID.randomUUID())
            .complete(true)
            .annualAverage(new BigDecimal("14.50"))
            .earnedCredits(6)
            .courses(
                List.of(
                    TranscriptCourseResponse.builder()
                        .courseId(UUID.randomUUID())
                        .ref("PROG1")
                        .title("Programming")
                        .credits(6)
                        .finalGrade(new BigDecimal("14.50"))
                        .validated(true)
                        .build()))
            .build();

    byte[] pdf = transcriptPdfService.generate(transcript);

    assertNotNull(pdf);
    assertTrue(pdf.length > 0);

    try (var document = Loader.loadPDF(pdf)) {
      assertEquals(1, document.getNumberOfPages());
    }
  }
}
