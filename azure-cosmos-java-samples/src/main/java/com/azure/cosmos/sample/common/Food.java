package com.azure.cosmos.sample.common;

public class Food {

    private String id;
    private String description;
    private String foodGroup;

    public Food() {
    }

    public Food(String id, String description, String foodGroup) {
        this.id = id;
        this.description = description;
        this.foodGroup = foodGroup;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFoodGroup() {
        return foodGroup;
    }

    public void setFoodGroup(String foodGroup) {
        this.foodGroup = foodGroup;
    }
}
