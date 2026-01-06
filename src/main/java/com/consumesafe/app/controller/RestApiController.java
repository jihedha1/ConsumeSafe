// src/main/java/com/consumesafe/app/controller/RestApiController.java
package com.consumesafe.app.controller;

import com.consumesafe.app.dto.CheckResult;
import com.consumesafe.app.model.Alternative;
import com.consumesafe.app.model.Product;
import com.consumesafe.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(result);
    }

    @GetMapping("/boycott-list")
    public ResponseEntity<List<Product>> getBoycottList() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
                .body(productService.getAllBoycottedProducts());
    }

    @GetMapping("/alternatives")
    public ResponseEntity<List<Alternative>> getAlternatives() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
                .body(productService.getAllAlternatives());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
                .body(productService.getProductsByCategory(category));
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<Product>> getBySeverity(@PathVariable String severity) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
                .body(productService.getProductsBySeverity(severity));
    }

    // Autocomplétion pour recherche
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String query) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(productService.searchSuggestions(query));
    }

    // Recherche floue
    @GetMapping("/search")
    public ResponseEntity<List<Product>> fuzzySearch(@RequestParam String query) {
        return ResponseEntity.ok()
                .body(productService.fuzzySearch(query));
    }

    // Catégories disponibles
    @GetMapping("/categories")
    public ResponseEntity<Set<String>> getCategories() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
                .body(productService.getAllCategories());
    }

    // Statistiques
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = Map.of(
                "totalProducts", productService.getAllBoycottedProducts().size(),
                "totalAlternatives", productService.getAllAlternatives().size(),
                "categoriesCount", productService.getCategoriesCount()
        );
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(stats);
    }
}