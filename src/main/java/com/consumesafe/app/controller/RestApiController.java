// src/main/java/com/consumesafe/app/controller/RestApiController.java
package com.consumesafe.app.controller;

import com.consumesafe.app.dto.CheckResult;
import com.consumesafe.app.model.Alternative;
import com.consumesafe.app.model.Product;
import com.consumesafe.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RestApiController {

    @Autowired
    private ProductService productService;

    @GetMapping("/check")
    public ResponseEntity<CheckResult> checkProduct(@RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        CheckResult result = productService.checkProduct(name);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/boycott-list")
    public ResponseEntity<List<Product>> getBoycottList() {
        return ResponseEntity.ok(productService.getAllBoycottedProducts());
    }

    @GetMapping("/alternatives")
    public ResponseEntity<List<Alternative>> getAlternatives() {
        return ResponseEntity.ok(productService.getAllAlternatives());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }
}