// src/main/java/com/consumesafe/app/controller/WebController.java
package com.consumesafe.app.controller;

import com.consumesafe.app.dto.CheckResult;
import com.consumesafe.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @PostMapping("/check")
    public String checkProduct(@RequestParam String productName, Model model) {
        if (productName == null || productName.trim().isEmpty()) {
            return "redirect:/";
        }

        CheckResult result = productService.checkProduct(productName);
        model.addAttribute("result", result);

        return "result";
    }

    @GetMapping("/list")
    public String listProducts(Model model) {
        model.addAttribute("boycottList", productService.getAllBoycottedProducts());
        return "list";
    }

    @GetMapping("/alternatives")
    public String alternatives(Model model) {
        model.addAttribute("alternatives", productService.getAllAlternatives());
        return "alternatives";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}