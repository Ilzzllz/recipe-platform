package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nutritional details of a single ingredient fetched from open food database")
public class IngredientNutritionDto {

    @Schema(description = "Ingredient name", example = "Tomato")
    private String ingredientName;

    @Schema(description = "Calories per 100g (kcal)", example = "18.0")
    private double caloriesKcal;

    @Schema(description = "Proteins per 100g (g)", example = "0.9")
    private double proteinsGrams;

    @Schema(description = "Fats per 100g (g)", example = "0.2")
    private double fatsGrams;

    @Schema(description = "Carbohydrates per 100g (g)", example = "3.9")
    private double carbohydratesGrams;

    @Schema(description = "Data source indicator", example = "Open Food Facts API")
    private String dataSource;
}
