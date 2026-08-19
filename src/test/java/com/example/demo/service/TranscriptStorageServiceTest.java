package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.demo.file.bucket.BucketComponent;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TranscriptStorageServiceTest {

  @Test
  void shouldUploadPdfAndReturnPresignedUrl() throws Exception {
    var bucketComponent = mock(BucketComponent.class);
    var service = new TranscriptStorageService(bucketComponent);

    UUID studentId = UUID.randomUUID();
    byte[] pdf = "%PDF-test".getBytes();

    URL expectedUrl = new URL("https://example.com/transcript.pdf");

    doAnswer(
            invocation -> {
              var file = invocation.<java.io.File>getArgument(0);

              assertTrue(file.exists());
              assertArrayEquals(pdf, Files.readAllBytes(file.toPath()));

              return null;
            })
        .when(bucketComponent)
        .upload(any(java.io.File.class), anyString());

    when(bucketComponent.presign(anyString(), any(Duration.class))).thenReturn(expectedUrl);

    URL result = service.store(studentId, pdf);

    assertEquals(expectedUrl, result);

    var keyCaptor = ArgumentCaptor.forClass(String.class);

    verify(bucketComponent).upload(any(java.io.File.class), keyCaptor.capture());

    String bucketKey = keyCaptor.getValue();

    assertTrue(bucketKey.startsWith("transcripts/" + studentId + "/"));
    assertTrue(bucketKey.endsWith(".pdf"));

    verify(bucketComponent).presign(bucketKey, Duration.ofHours(24));
  }
}
