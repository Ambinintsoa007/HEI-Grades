package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.conf.JwtConf;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

  @Test
  void shouldGenerateValidJwt() {
    JwtConf jwtConf = new JwtConf();

    var secretKey = jwtConf.jwtSecretKey("test-secret-key-that-is-long-enough-for-jwt-tests");

    var encoder = jwtConf.jwtEncoder(secretKey);
    var decoder = jwtConf.jwtDecoder(secretKey);

    JwtService jwtService = new JwtService(encoder);
    ReflectionTestUtils.setField(jwtService, "expirationSeconds", 3600L);

    UUID userId = UUID.randomUUID();

    String token = jwtService.generateToken(userId, "ADMIN");

    var jwt = decoder.decode(token);

    assertEquals(userId.toString(), jwt.getSubject());
    assertEquals("ADMIN", jwt.getClaimAsString("role"));
    assertNotNull(jwt.getIssuedAt());
    assertNotNull(jwt.getExpiresAt());
  }
}
