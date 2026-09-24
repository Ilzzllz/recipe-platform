package com.example.recipeplatform.mapper;

import com.example.recipeplatform.dto.IngredientCreateDto;
import com.example.recipeplatform.dto.IngredientDto;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.util.TextNormalizer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IngredientMapper {

    public IngredientDto toDto(Ingredient ingredient) {
        if (ingredient == null) {
            return null;
        }

        IngredientDto dto = new IngredientDto();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        dto.setCaloriesPer100g(ingredient.getCaloriesPer100g());
        dto.setProteinsPer100g(ingredient.getProteinsPer100g());
        dto.setFatsPer100g(ingredient.getFatsPer100g());
        dto.setCarbohydratesPer100g(ingredient.getCarbohydratesPer100g());
        dto.setGramsPerUnit(ingredient.getGramsPerUnit());
        return dto;
    }

    public Ingredient toEntity(IngredientCreateDto dto) {
        Ingredient ingredient = new Ingredient();
        updateEntity(ingredient, dto);
        return ingredient;
    }

    public void updateEntity(Ingredient ingredient, IngredientCreateDto dto) {
        ingredient.setName(TextNormalizer.normalize(dto.getName()));
        ingredient.setCaloriesPer100g(dto.getCaloriesPer100g());
        ingredient.setProteinsPer100g(dto.getProteinsPer100g());
        ingredient.setFatsPer100g(dto.getFatsPer100g());
        ingredient.setCarbohydratesPer100g(dto.getCarbohydratesPer100g());
        ingredient.setGramsPerUnit(dto.getGramsPerUnit());
    }

    public List<IngredientDto> toDtoList(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(this::toDto)
                .toList();
    }
}
