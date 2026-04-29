package com.example.recipeplatform.mapper;

import com.example.recipeplatform.dto.RecipeCreateDto;
import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.model.Recipe;
import org.springframework.stereotype.Component;

import java.util.List;

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
        dto.setIngredients(recipe.getIngredients().stream()
                .map(ingredientMapper::toDto)
                .toList());
        dto.setSteps(recipe.getSteps().stream()
                .map(cookingStepMapper::toDto)
                .toList());
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
}
