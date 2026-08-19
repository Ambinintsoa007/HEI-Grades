package com.example.demo.service;

import com.example.demo.mail.Email;
import com.example.demo.mail.Mailer;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.net.URL;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TranscriptEmailService {

  private final Mailer mailer;

  public void send(String recipientEmail, URL transcriptUrl) {
    try {
      var email =
          new Email(
              new InternetAddress(recipientEmail),
              List.of(),
              List.of(),
              "Your HEI transcript",
              """
              <p>Hello,</p>
              <p>Your HEI transcript is ready.</p>
              <p>
                <a href="%s">Download your transcript</a>
              </p>
              <p>This link is valid for 24 hours.</p>
              """
                  .formatted(transcriptUrl),
              List.of());

      mailer.accept(email);

    } catch (AddressException e) {
      throw new IllegalArgumentException("Invalid student email address", e);
    }
  }
}
