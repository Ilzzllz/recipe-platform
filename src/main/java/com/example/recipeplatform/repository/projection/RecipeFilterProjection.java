package com.example.recipeplatform.repository.projection;

public interface RecipeFilterProjection {

    Long getRecipeId();

    String getRecipeTitle();

    String getRecipeDescription();

    Long getAuthorId();

    String getAuthorUsername();

    Long getCategoryId();

    String getCategoryName();
}
