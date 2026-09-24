package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "Ingredient create or update request")
public class IngredientCreateDto {

    @NotBlank
    @Schema(description = "Ingredient name", example = "Olive oil")
    private String name;

    @DecimalMin(value = "0.0")
    private BigDecimal caloriesPer100g = BigDecimal.ZERO;

    @DecimalMin(value = "0.0")
    private BigDecimal proteinsPer100g = BigDecimal.ZERO;

    @DecimalMin(value = "0.0")
    private BigDecimal fatsPer100g = BigDecimal.ZERO;

    @DecimalMin(value = "0.0")
    private BigDecimal carbohydratesPer100g = BigDecimal.ZERO;

    @DecimalMin(value = "0.001")
    private BigDecimal gramsPerUnit = BigDecimal.ONE;
}
