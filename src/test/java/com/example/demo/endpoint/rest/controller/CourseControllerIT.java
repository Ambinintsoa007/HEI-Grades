package com.example.demo.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.CourseManagementTestBase;
import com.example.demo.endpoint.rest.dto.CourseResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;

class CourseControllerIT extends CourseManagementTestBase {

  @Autowired private TestRestTemplate restTemplate;

  @BeforeEach
  void enablePatchSupport() {
    restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
  }

  @Test
  void create_course_returns_201() {
    var request = Map.of("ref", "CRS-CTRL-1", "title", "Algo", "credits", 5);

    ResponseEntity<CourseResponse> response =
        restTemplate.postForEntity("/courses", request, CourseResponse.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().id());
    assertEquals("CRS-CTRL-1", response.getBody().ref());
    assertEquals("Algo", response.getBody().title());
    assertEquals(5, response.getBody().credits());
  }

  @Test
  void create_course_rejects_credits_zero() {
    var request = Map.of("ref", "CRS-CTRL-2", "title", "Algo", "credits", 0);

    ResponseEntity<Void> response = restTemplate.postForEntity("/courses", request, Void.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void create_course_rejects_duplicate_ref() {
    var request = Map.of("ref", "CRS-CTRL-3", "title", "Algo", "credits", 5);
    restTemplate.postForEntity("/courses", request, CourseResponse.class);

    ResponseEntity<Void> response = restTemplate.postForEntity("/courses", request, Void.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void list_courses_returns_200() {
    var request = Map.of("ref", "CRS-CTRL-4", "title", "Algo", "credits", 5);
    var created = restTemplate.postForEntity("/courses", request, CourseResponse.class).getBody();

    ResponseEntity<CourseResponse[]> response =
        restTemplate.getForEntity("/courses", CourseResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(
        Arrays.stream(response.getBody()).anyMatch(course -> course.id().equals(created.id())));
  }

  @Test
  void update_course_returns_200() {
    var createRequest = Map.of("ref", "CRS-CTRL-5", "title", "Algo", "credits", 5);
    var created =
        restTemplate.postForEntity("/courses", createRequest, CourseResponse.class).getBody();
    var patchRequest = Map.of("credits", 8);

    ResponseEntity<CourseResponse> response =
        restTemplate.exchange(
            "/courses/" + created.id(),
            HttpMethod.PATCH,
            new HttpEntity<>(patchRequest),
            CourseResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(8, response.getBody().credits());
    assertEquals("CRS-CTRL-5", response.getBody().ref());
  }

  @Test
  void update_course_not_found_returns_404() {
    var patchRequest = Map.of("title", "X");

    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/courses/" + UUID.randomUUID(),
            HttpMethod.PATCH,
            new HttpEntity<>(patchRequest),
            Void.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }
}
