package com.example.recipeplatform.mapper;

import com.example.recipeplatform.dto.CookingStepDto;
import com.example.recipeplatform.dto.IngredientDto;
import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.model.CookingStep;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class RecipeMapper {

    public RecipeDto toDto(Recipe recipe) {
        if (recipe == null) {
            return null;
        }

        RecipeDto dto = new RecipeDto();
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        dto.setAuthorId(recipe.getAuthor().getId());
        dto.setAuthorUsername(recipe.getAuthor().getUsername());
        dto.setCategoryId(recipe.getCategory().getId());
        dto.setCategoryName(recipe.getCategory().getName());
        dto.setIngredients(recipe.getIngredients().stream()
                .map(this::toIngredientDto)
                .sorted(Comparator.comparing(IngredientDto::getName))
                .toList());
        dto.setSteps(recipe.getSteps().stream()
                .sorted(Comparator.comparing(CookingStep::getStepOrder))
                .map(this::toStepDto)
                .toList());
        return dto;
    }

    public CookingStepDto toStepDto(CookingStep step) {
        CookingStepDto dto = new CookingStepDto();
        dto.setId(step.getId());
        dto.setStepOrder(step.getStepOrder());
        dto.setDescription(step.getDescription());
        dto.setRecipeId(step.getRecipe().getId());
        return dto;
    }

    public IngredientDto toIngredientDto(Ingredient ingredient) {
        IngredientDto dto = new IngredientDto();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        return dto;
    }

    public List<RecipeDto> toDtoList(List<Recipe> recipes) {
        return recipes.stream()
                .map(this::toDto)
                .toList();
    }
}
