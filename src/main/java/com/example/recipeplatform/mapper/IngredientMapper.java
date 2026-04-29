package com.example.recipeplatform.mapper;

import com.example.recipeplatform.dto.IngredientCreateDto;
import com.example.recipeplatform.dto.IngredientDto;
import com.example.recipeplatform.model.Ingredient;
import org.springframework.stereotype.Component;

@Component
public class IngredientMapper {

    public IngredientDto toDto(Ingredient ingredient) {
        if (ingredient == null) {
            return null;
        }

        IngredientDto dto = new IngredientDto();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        return dto;
    }

    public Ingredient toEntity(IngredientCreateDto dto) {
        Ingredient ingredient = new Ingredient();
        updateEntity(ingredient, dto);
        return ingredient;
    }

    public void updateEntity(Ingredient ingredient, IngredientCreateDto dto) {
        ingredient.setName(dto.getName());
    }
}
