package com.example.recipeplatform.mapper;

import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.model.Recipe;
import org.springframework.stereotype.Component;

@Component
public class RecipeMapper {
    public RecipeDto toDto(Recipe recipe) {
        RecipeDto dto = new RecipeDto();
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        return dto;
    }
}
