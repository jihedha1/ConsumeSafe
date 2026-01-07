// src/main/java/com/consumesafe/app/dto/CheckResult.java
package com.consumesafe.app.dto;

public class CheckResult {
    private String productName;
    private boolean isBoycotted;
    private String suggestion;
    private String message;
    private String reason;
    private String severity; // "high", "medium", "low", "safe", "unknown"
    private boolean productFound; // Nouveau champ pour indiquer si le produit existe dans la base

    public CheckResult(String productName) {
        this.productName = productName;
        this.isBoycotted = false;
        this.severity = "unknown";
        this.productFound = true; // Par défaut
    }

    // Getters
    public String getProductName() {
        return productName;
    }

    public boolean isBoycotted() {
        return isBoycotted;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public String getMessage() {
        return message;
    }

    public String getReason() {
        return reason;
    }

    public String getSeverity() {
        return severity;
    }

    public boolean isProductFound() {
        return productFound;
    }

    // Setters
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setBoycotted(boolean boycotted) {
        isBoycotted = boycotted;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public void setProductFound(boolean productFound) {
        this.productFound = productFound;
    }

    // Méthode utilitaire pour déterminer le type de résultat
    public String getResultType() {
        if (!productFound) {
            return "unknown";
        }
        if (isBoycotted) {
            return "boycotted";
        }
        return "safe";
    }

    @Override
    public String toString() {
        return "CheckResult{" +
                "productName='" + productName + '\'' +
                ", isBoycotted=" + isBoycotted +
                ", severity='" + severity + '\'' +
                ", productFound=" + productFound +
                ", message='" + message + '\'' +
                '}';
    }
}