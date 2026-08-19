package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.mail.Email;
import com.example.demo.mail.Mailer;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TranscriptEmailServiceTest {

  @Test
  void shouldSendTranscriptLinkByEmail() throws Exception {
    var mailer = mock(Mailer.class);
    var service = new TranscriptEmailService(mailer);

    var transcriptUrl = URI.create("https://example.com/transcript.pdf").toURL();

    service.send("student@example.com", transcriptUrl);

    var emailCaptor = ArgumentCaptor.forClass(Email.class);

    verify(mailer, times(1)).accept(emailCaptor.capture());

    var email = emailCaptor.getValue();

    assertEquals("student@example.com", email.to().getAddress());
    assertEquals("Your HEI transcript", email.subject());

    assertTrue(email.htmlBody().contains(transcriptUrl.toString()));
    assertTrue(email.htmlBody().contains("24 hours"));

    assertTrue(email.cc().isEmpty());
    assertTrue(email.bcc().isEmpty());
    assertTrue(email.attachments().isEmpty());
  }
}
