package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Recipe response")
public class RecipeDto {

    @Schema(description = "Recipe id", example = "12")
    private Long id;

    @Schema(description = "Recipe title", example = "Borscht")
    private String title;

    @Schema(description = "Recipe description", example = "Classic beet soup with vegetables and sour cream")
    private String description;
    private Integer portions;

    @Schema(description = "Author information")
    private AuthorReferenceDto author;

    @Schema(description = "Category information")
    private CategoryReferenceDto category;

    private List<IngredientDto> ingredients;
    private List<RecipeIngredientDto> recipeIngredients;
    private List<CookingStepDto> steps;
    private NutritionSummaryDto nutrition;
}
