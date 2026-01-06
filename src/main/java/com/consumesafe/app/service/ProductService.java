// src/main/java/com/consumesafe/app/service/ProductService.java
package com.consumesafe.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.consumesafe.app.dto.CheckResult;
import com.consumesafe.app.model.Product;
import com.consumesafe.app.model.Alternative;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private List<Product> boycottList;
    private List<Alternative> alternativesList;
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        loadData();
    }

    private void loadData() {
        try {
            File boycottFile = ResourceUtils.getFile("classpath:boycott-list.json");
            File alternativesFile = ResourceUtils.getFile("classpath:alternatives.json");

            boycottList = mapper.readValue(boycottFile, new TypeReference<List<Product>>() {});
            alternativesList = mapper.readValue(alternativesFile, new TypeReference<List<Alternative>>() {});
        } catch (IOException e) {
            boycottList = List.of();
            alternativesList = List.of();
            System.err.println("Erreur lors du chargement des données: " + e.getMessage());
        }
    }

    @Cacheable(value = "searchResults", key = "#productName")
    public CheckResult checkProduct(String productName) {
        CheckResult result = new CheckResult(productName);

        Product foundProduct = boycottList.stream()
                .filter(item -> item.getName().equalsIgnoreCase(productName.trim()))
                .findFirst()
                .orElse(null);

        if (foundProduct != null) {
            result.setBoycotted(true);
            result.setMessage("⚠️ Ce produit est sur la liste de boycott");
            result.setReason(foundProduct.getReason());
            result.setSeverity(foundProduct.getSeverity());

            List<Alternative> sameCategory = alternativesList.stream()
                    .filter(alt -> alt.getCategory().equalsIgnoreCase(foundProduct.getCategory()))
                    .collect(Collectors.toList());

            if (!sameCategory.isEmpty()) {
                Random rand = new Random();
                Alternative suggestion = sameCategory.get(rand.nextInt(sameCategory.size()));
                result.setSuggestion(suggestion.getName() + " - " + suggestion.getDescription());
            }
        } else {
            result.setBoycotted(false);
            result.setMessage("✓ Ce produit ne semble pas être sur la liste de boycott");
            result.setSeverity("safe");
        }

        return result;
    }

    @Cacheable("products")
    public List<Product> getAllBoycottedProducts() {
        return new ArrayList<>(boycottList);
    }

    @Cacheable("alternatives")
    public List<Alternative> getAllAlternatives() {
        return new ArrayList<>(alternativesList);
    }

    public List<Product> getProductsByCategory(String category) {
        return boycottList.stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<Product> getProductsBySeverity(String severity) {
        return boycottList.stream()
                .filter(p -> p.getSeverity().equalsIgnoreCase(severity))
                .collect(Collectors.toList());
    }

    // Recherche avancée avec suggestions
    public List<String> searchSuggestions(String query) {
        if (query == null || query.length() < 2) {
            return Collections.emptyList();
        }

        String lowerQuery = query.toLowerCase();

        return boycottList.stream()
                .map(Product::getName)
                .filter(name -> name.toLowerCase().contains(lowerQuery))
                .limit(5)
                .collect(Collectors.toList());
    }

    // Recherche floue (correction orthographique basique)
    public List<Product> fuzzySearch(String query) {
        String lowerQuery = query.toLowerCase();

        return boycottList.stream()
                .filter(p -> {
                    String name = p.getName().toLowerCase();
                    return name.contains(lowerQuery) ||
                            levenshteinDistance(name, lowerQuery) <= 2;
                })
                .collect(Collectors.toList());
    }

    // Distance de Levenshtein pour correction orthographique
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                                dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    public Set<String> getAllCategories() {
        return boycottList.stream()
                .map(Product::getCategory)
                .collect(Collectors.toSet());
    }

    public Map<String, Long> getCategoriesCount() {
        return boycottList.stream()
                .collect(Collectors.groupingBy(
                        Product::getCategory,
                        Collectors.counting()
                ));
    }
}