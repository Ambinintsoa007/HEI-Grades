package com.example.demo.endpoint.rest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPageController {

  @GetMapping("/web/login")
  public String login() {
    return "login";
  }

  @GetMapping("/web/student")
  public String student() {
    return "student";
  }

  @GetMapping("/web/teacher")
  public String teacher() {
    return "teacher";
  }
}
