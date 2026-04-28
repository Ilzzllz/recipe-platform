package com.example.recipeplatform.dto;

import java.util.List;

public class NPlusOneDemoResponse {

    private String scenario;
    private long sqlStatements;
    private long recipesLoaded;
    private List<RecipeDto> recipes;

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public long getSqlStatements() {
        return sqlStatements;
    }

    public void setSqlStatements(long sqlStatements) {
        this.sqlStatements = sqlStatements;
    }

    public long getRecipesLoaded() {
        return recipesLoaded;
    }

    public void setRecipesLoaded(long recipesLoaded) {
        this.recipesLoaded = recipesLoaded;
    }

    public List<RecipeDto> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<RecipeDto> recipes) {
        this.recipes = recipes;
    }
}
