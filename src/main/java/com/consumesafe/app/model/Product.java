// src/main/java/com/consumesafe/app/model/Product.java
package com.consumesafe.app.model;

public class Product {
    private String name;
    private String reason;
    private String severity;
    private String category;

    public Product() {}

    public Product(String name, String reason, String severity, String category) {
        this.name = name;
        this.reason = reason;
        this.severity = severity;
        this.category = category;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
