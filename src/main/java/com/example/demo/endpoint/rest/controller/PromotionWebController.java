package com.example.demo.endpoint.rest.controller;

import com.example.demo.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PromotionWebController {

  private final PromotionService promotionService;

  @GetMapping("/web/promotions")
  public String promotions(Model model) {
    model.addAttribute("promotions", promotionService.getAll());
    return "promotions";
  }
}
