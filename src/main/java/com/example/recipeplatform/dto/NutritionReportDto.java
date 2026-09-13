package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "Aggregated nutritional report for a recipe computed asynchronously")
public class NutritionReportDto {

    @Schema(description = "Recipe ID", example = "1")
    private Long recipeId;

    @Schema(description = "Recipe title", example = "Pumpkin Cream Soup")
    private String recipeTitle;

    @Schema(description = "Total estimated energy in kcal", example = "350.5")
    private double totalCaloriesKcal;

    @Schema(description = "Total proteins in grams", example = "12.4")
    private double totalProteinsGrams;

    @Schema(description = "Total fats in grams", example = "15.2")
    private double totalFatsGrams;

    @Schema(description = "Total carbohydrates in grams", example = "42.1")
    private double totalCarbohydratesGrams;

    @Schema(description = "Nutritional breakdown per ingredient")
    private List<IngredientNutritionDto> ingredients;

    @Schema(description = "Timestamp when calculation was completed")
    private LocalDateTime calculatedAt;
}
