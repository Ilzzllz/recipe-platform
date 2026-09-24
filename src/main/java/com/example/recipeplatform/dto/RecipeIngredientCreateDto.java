package com.example.recipeplatform.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RecipeIngredientCreateDto {
    @NotNull
    private Long ingredientId;

    @NotNull
    @DecimalMin(value = "0.001")
    private BigDecimal quantity;

    @NotNull
    @Size(min = 1, max = 16)
    private String unit;
}
