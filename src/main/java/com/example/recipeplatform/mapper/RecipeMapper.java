package com.example.recipeplatform.mapper;

import com.example.recipeplatform.dto.RecipeCreateDto;
import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.model.CookingStep;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class RecipeMapper {

    private final IngredientMapper ingredientMapper;
    private final CookingStepMapper cookingStepMapper;

    public RecipeMapper(IngredientMapper ingredientMapper, CookingStepMapper cookingStepMapper) {
        this.ingredientMapper = ingredientMapper;
        this.cookingStepMapper = cookingStepMapper;
    }

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
        dto.setIngredients(uniqueIngredients(recipe));
        dto.setSteps(uniqueSteps(recipe));
        return dto;
    }

    public Recipe toEntity(RecipeCreateDto dto) {
        Recipe recipe = new Recipe();
        updateEntity(recipe, dto);
        return recipe;
    }

    public void updateEntity(Recipe recipe, RecipeCreateDto dto) {
        recipe.setTitle(dto.getTitle());
        recipe.setDescription(dto.getDescription());
    }

    public List<RecipeDto> toDtoList(List<Recipe> recipes) {
        return recipes.stream()
                .map(this::toDto)
                .toList();
    }

    private List<com.example.recipeplatform.dto.IngredientDto> uniqueIngredients(Recipe recipe) {
        Map<Long, Ingredient> uniqueIngredients = new LinkedHashMap<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            uniqueIngredients.putIfAbsent(ingredient.getId(), ingredient);
        }
        return uniqueIngredients.values().stream()
                .map(ingredientMapper::toDto)
                .toList();
    }

    private List<com.example.recipeplatform.dto.CookingStepDto> uniqueSteps(Recipe recipe) {
        Map<Long, CookingStep> uniqueSteps = new LinkedHashMap<>();
        for (CookingStep step : recipe.getSteps()) {
            uniqueSteps.putIfAbsent(step.getId(), step);
        }
        return uniqueSteps.values().stream()
                .sorted(Comparator.comparing(CookingStep::getStepOrder))
                .map(cookingStepMapper::toDto)
                .toList();
    }
}
