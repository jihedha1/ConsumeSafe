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

        // 1. Vérifier d'abord si le produit est dans la liste de BOYCOTT
        Product foundProduct = boycottList.stream()
                .filter(item -> item.getName().equalsIgnoreCase(productName.trim()))
                .findFirst()
                .orElse(null);

        if (foundProduct != null) {
            // Produit trouvé dans la liste de boycott
            result.setBoycotted(true);
            result.setMessage("⚠️ Ce produit est sur la liste de boycott");
            result.setReason(foundProduct.getReason());
            result.setSeverity(foundProduct.getSeverity());
            result.setProductFound(true);

            // Suggestion d'alternatives
            List<Alternative> sameCategory = alternativesList.stream()
                    .filter(alt -> alt.getCategory().equalsIgnoreCase(foundProduct.getCategory()))
                    .collect(Collectors.toList());

            if (!sameCategory.isEmpty()) {
                Random rand = new Random();
                Alternative suggestion = sameCategory.get(rand.nextInt(sameCategory.size()));
                result.setSuggestion(suggestion.getName() + " - " + suggestion.getDescription());
            }
            return result;
        }

        // 2. Vérifier si le produit est dans la liste des ALTERNATIVES (produits sûrs/tunisiens)
        Alternative foundAlternative = alternativesList.stream()
                .filter(alt -> alt.getName().equalsIgnoreCase(productName.trim()))
                .findFirst()
                .orElse(null);

        if (foundAlternative != null) {
            // Produit trouvé dans les alternatives - C'EST UN PRODUIT SÛR !
            result.setBoycotted(false);
            result.setProductFound(true);
            result.setMessage("✅ Excellent choix ! Ce produit est une alternative tunisienne recommandée 🇹🇳");
            result.setSeverity("safe");
            result.setReason("🇹🇳 " + foundAlternative.getDescription() +
                    "\n\nEn choisissant ce produit tunisien, vous :\n" +
                    "✓ Soutenez l'économie locale\n" +
                    "✓ Créez des emplois en Tunisie\n" +
                    "✓ Garantissez la qualité Made in Tunisia\n" +
                    "✓ Exprimez votre solidarité avec la Palestine");

            // Suggérer d'autres alternatives de la même catégorie
            List<Alternative> sameCategory = alternativesList.stream()
                    .filter(alt -> alt.getCategory().equalsIgnoreCase(foundAlternative.getCategory())
                            && !alt.getName().equalsIgnoreCase(productName.trim()))
                    .collect(Collectors.toList());

            if (!sameCategory.isEmpty()) {
                Random rand = new Random();
                Alternative otherSuggestion = sameCategory.get(rand.nextInt(sameCategory.size()));
                result.setSuggestion("💡 Autres produits tunisiens dans la catégorie " +
                        foundAlternative.getCategory() + " : " +
                        otherSuggestion.getName() + " - " + otherSuggestion.getDescription());
            } else {
                result.setSuggestion("🇹🇳 Continuez à soutenir les produits tunisiens ! Consultez notre liste complète d'alternatives.");
            }
            return result;
        }

        // 3. Le produit n'est ni dans la liste de boycott, ni dans les alternatives
        // Recherche floue dans la liste de boycott ET les alternatives
        List<Product> similarBoycottProducts = fuzzySearch(productName);
        List<Alternative> similarAlternatives = fuzzySearchAlternatives(productName);

        if (!similarBoycottProducts.isEmpty() || !similarAlternatives.isEmpty()) {
            // Des produits similaires existent - suggérer à l'utilisateur
            result.setBoycotted(false);
            result.setProductFound(false);
            result.setSeverity("unknown");

            StringBuilder suggestions = new StringBuilder("❓ Produit non trouvé. Vouliez-vous dire :\n");

            if (!similarBoycottProducts.isEmpty()) {
                suggestions.append("\n⚠️ Produits à boycotter : ")
                        .append(similarBoycottProducts.stream()
                                .limit(2)
                                .map(Product::getName)
                                .collect(Collectors.joining(", ")));
            }

            if (!similarAlternatives.isEmpty()) {
                suggestions.append("\n✅ Alternatives tunisiennes : ")
                        .append(similarAlternatives.stream()
                                .limit(2)
                                .map(Alternative::getName)
                                .collect(Collectors.joining(", ")));
            }

            result.setMessage(suggestions.toString());
            result.setReason("Ce produit n'est pas dans notre base de données. Veuillez vérifier l'orthographe ou consulter la liste complète.");
        } else {
            // Aucun produit similaire trouvé
            result.setBoycotted(false);
            result.setProductFound(false);
            result.setMessage("❓ Produit inconnu - Non répertorié dans notre base de données");
            result.setSeverity("unknown");
            result.setReason("⚠️ ATTENTION : Ce produit n'est pas dans notre base de données actuelle. " +
                    "Cela ne signifie pas qu'il est sûr à consommer. " +
                    "\n\nNous vous recommandons de :\n" +
                    "• Vérifier la liste complète des produits boycottés\n" +
                    "• Consulter nos alternatives tunisiennes\n" +
                    "• Rechercher l'origine et les liens du fabricant\n" +
                    "• Privilégier les alternatives tunisiennes pour plus de sécurité\n" +
                    "• Nous contacter si vous avez des informations sur ce produit");

            // Suggérer des alternatives générales
            if (!alternativesList.isEmpty()) {
                Random rand = new Random();
                Alternative suggestion = alternativesList.get(rand.nextInt(alternativesList.size()));
                result.setSuggestion("💡 Conseil : Privilégiez les produits tunisiens comme " +
                        suggestion.getName() + " - " + suggestion.getDescription());
            }
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

    // Recherche floue avec distance de Levenshtein améliorée
    public List<Product> fuzzySearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String lowerQuery = query.toLowerCase().trim();

        // Seuil de distance adaptatif basé sur la longueur de la requête
        int threshold = query.length() <= 5 ? 1 : (query.length() <= 10 ? 2 : 3);

        return boycottList.stream()
                .filter(p -> {
                    String name = p.getName().toLowerCase();
                    // Recherche exacte par sous-chaîne
                    if (name.contains(lowerQuery) || lowerQuery.contains(name)) {
                        return true;
                    }
                    // Recherche avec distance de Levenshtein
                    return levenshteinDistance(name, lowerQuery) <= threshold;
                })
                .sorted((p1, p2) -> {
                    // Trier par pertinence (distance de Levenshtein)
                    int dist1 = levenshteinDistance(p1.getName().toLowerCase(), lowerQuery);
                    int dist2 = levenshteinDistance(p2.getName().toLowerCase(), lowerQuery);
                    return Integer.compare(dist1, dist2);
                })
                .limit(5)
                .collect(Collectors.toList());
    }

    // Recherche floue dans les ALTERNATIVES
    public List<Alternative> fuzzySearchAlternatives(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String lowerQuery = query.toLowerCase().trim();
        int threshold = query.length() <= 5 ? 1 : (query.length() <= 10 ? 2 : 3);

        return alternativesList.stream()
                .filter(alt -> {
                    String name = alt.getName().toLowerCase();
                    if (name.contains(lowerQuery) || lowerQuery.contains(name)) {
                        return true;
                    }
                    return levenshteinDistance(name, lowerQuery) <= threshold;
                })
                .sorted((a1, a2) -> {
                    int dist1 = levenshteinDistance(a1.getName().toLowerCase(), lowerQuery);
                    int dist2 = levenshteinDistance(a2.getName().toLowerCase(), lowerQuery);
                    return Integer.compare(dist1, dist2);
                })
                .limit(5)
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
                                dp[i - 1][j] + 1,      // Suppression
                                dp[i][j - 1] + 1),     // Insertion
                        dp[i - 1][j - 1] + cost        // Substitution
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

    // Méthode pour vérifier si un produit existe dans la base
    public boolean productExists(String productName) {
        return boycottList.stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(productName.trim()));
    }
}