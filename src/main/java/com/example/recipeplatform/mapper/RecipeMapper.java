package com.example.recipeplatform.mapper;

import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.model.Recipe;
import org.springframework.stereotype.Component;

@Component
public class RecipeMapper {
    public RecipeDto toDto(Recipe recipe) {
        if (recipe == null) {
            return null;
        }
        return new RecipeDto(
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getCategory()
        );
    }
}
