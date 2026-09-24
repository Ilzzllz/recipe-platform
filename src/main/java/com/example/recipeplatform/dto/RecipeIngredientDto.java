package com.example.recipeplatform.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RecipeIngredientDto {
    private IngredientDto ingredient;
    private BigDecimal quantity;
    private String unit;
}
