// src/main/java/com/consumesafe/app/service/ProductService.java
package com.consumesafe.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.consumesafe.app.dto.CheckResult;
import com.consumesafe.app.model.Product;
import com.consumesafe.app.model.Alternative;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
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
            e.printStackTrace();
        }
    }

    public CheckResult checkProduct(String productName) {
        CheckResult result = new CheckResult(productName);

        // Recherche du produit (insensible à la casse)
        Product foundProduct = boycottList.stream()
                .filter(item -> item.getName().equalsIgnoreCase(productName.trim()))
                .findFirst()
                .orElse(null);

        if (foundProduct != null) {
            result.setBoycotted(true);
            result.setMessage("⚠️ Ce produit est sur la liste de boycott");
            result.setReason(foundProduct.getReason());
            result.setSeverity(foundProduct.getSeverity());

            // Suggérer une alternative de la même catégorie si possible
            List<Alternative> sameCategory = alternativesList.stream()
                    .filter(alt -> alt.getCategory().equalsIgnoreCase(foundProduct.getCategory()))
                    .collect(Collectors.toList());

            if (!sameCategory.isEmpty()) {
                Random rand = new Random();
                Alternative suggestion = sameCategory.get(rand.nextInt(sameCategory.size()));
                result.setSuggestion(suggestion.getName() + " - " + suggestion.getDescription());
            } else if (!alternativesList.isEmpty()) {
                // Suggérer une alternative aléatoire si pas de catégorie correspondante
                Random rand = new Random();
                Alternative suggestion = alternativesList.get(rand.nextInt(alternativesList.size()));
                result.setSuggestion(suggestion.getName() + " - " + suggestion.getDescription());
            }
        } else {
            result.setBoycotted(false);
            result.setMessage("✓ Ce produit ne semble pas être sur la liste de boycott");
            result.setSeverity("safe");
        }

        return result;
    }

    public List<Product> getAllBoycottedProducts() {
        return boycottList;
    }

    public List<Alternative> getAllAlternatives() {
        return alternativesList;
    }

    public List<Product> getProductsByCategory(String category) {
        return boycottList.stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }
}