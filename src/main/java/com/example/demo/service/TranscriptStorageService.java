package com.example.demo.service;

import com.example.demo.file.bucket.BucketComponent;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TranscriptStorageService {

  private static final Duration LINK_EXPIRATION = Duration.ofHours(24);

  private final BucketComponent bucketComponent;

  public URL store(UUID studentId, byte[] pdf) {
    var bucketKey = "transcripts/" + studentId + "/" + UUID.randomUUID() + ".pdf";

    java.nio.file.Path tempFile = null;

    try {
      tempFile = Files.createTempFile("hei-transcript-", ".pdf");
      Files.write(tempFile, pdf);

      bucketComponent.upload(tempFile.toFile(), bucketKey);

      return bucketComponent.presign(bucketKey, LINK_EXPIRATION);

    } catch (IOException e) {
      throw new IllegalStateException("Failed to store transcript PDF", e);

    } finally {
      if (tempFile != null) {
        try {
          Files.deleteIfExists(tempFile);
        } catch (IOException ignored) {
          // Temporary file cleanup failure must not fail transcript delivery.
        }
      }
    }
  }
}
