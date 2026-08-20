package com.example.demo.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.CourseManagementTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

class WebPageControllerIT extends CourseManagementTestBase {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void loginPageRenders() {
    var response = restTemplate.getForEntity("/web/login", String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("Connexion"));
    assertTrue(response.getBody().contains("Se connecter"));
  }

  @Test
  void studentPageRenders() {
    var response = restTemplate.getForEntity("/web/student", String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("Mes notes"));
    assertTrue(response.getBody().contains("role-badge\">STUDENT"));
    assertTrue(response.getBody().contains("Mon relevé"));
    assertTrue(response.getBody().contains("Changer de compte"));
  }

  @Test
  void teacherPageRenders() {
    var response = restTemplate.getForEntity("/web/teacher", String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("Mes cours"));
    assertTrue(response.getBody().contains("role-badge\">TEACHER"));
    assertTrue(response.getBody().contains("Motif de correction"));
    assertTrue(response.getBody().contains("Changer de compte"));
  }

  @Test
  void staticAssetsAreServed() {
    var js = restTemplate.getForEntity("/js/auth.js", String.class);
    var css = restTemplate.getForEntity("/css/app.css", String.class);

    assertEquals(HttpStatus.OK, js.getStatusCode());
    assertTrue(js.getBody().contains("apiFetch"));
    assertTrue(js.getBody().contains("localStorage"));
    assertEquals(HttpStatus.OK, css.getStatusCode());
    assertTrue(css.getBody().contains(".container"));
    assertTrue(css.getBody().contains(".role-badge"));
  }
}
