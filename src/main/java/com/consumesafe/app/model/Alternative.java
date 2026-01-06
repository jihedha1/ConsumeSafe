// src/main/java/com/consumesafe/app/model/Alternative.java
package com.consumesafe.app.model;

public class Alternative {
    private String name;
    private String description;
    private String category;
    private boolean isTunisian;

    public Alternative() {}

    public Alternative(String name, String description, String category, boolean isTunisian) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.isTunisian = isTunisian;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isTunisian() {
        return isTunisian;
    }

    // Le setter doit être corrigé comme ceci
    public void setIsTunisian(boolean isTunisian) {
        this.isTunisian = isTunisian;
    }
}
