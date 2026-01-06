// src/main/java/com/consumesafe/app/dto/CheckResult.java
package com.consumesafe.app.dto;

public class CheckResult {
    private String productName;
    private boolean isBoycotted;
    private String suggestion;
    private String message;
    private String reason;
    private String severity; // "high", "medium", "low"

    public CheckResult(String productName) {
        this.productName = productName;
        this.isBoycotted = false;
        this.severity = "low";
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
}