package com.example.recipeplatform.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class NutritionSummaryDto {
    private BigDecimal totalWeightGrams;
    private BigDecimal caloriesKcal;
    private BigDecimal proteinsGrams;
    private BigDecimal fatsGrams;
    private BigDecimal carbohydratesGrams;
    private BigDecimal caloriesPer100g;
    private BigDecimal proteinsPer100g;
    private BigDecimal fatsPer100g;
    private BigDecimal carbohydratesPer100g;
    private BigDecimal caloriesPerPortion;
    private BigDecimal proteinsPerPortion;
    private BigDecimal fatsPerPortion;
    private BigDecimal carbohydratesPerPortion;
    private Integer portions;
}
