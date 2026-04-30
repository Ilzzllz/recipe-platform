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
    @Schema(description = "Author id", example = "1")
    private Long authorId;
    @Schema(description = "Author username", example = "anna")
    private String authorUsername;
    @Schema(description = "Category id", example = "1")
    private Long categoryId;
    @Schema(description = "Category name", example = "Soups")
    private String categoryName;
    private List<IngredientDto> ingredients;
    private List<CookingStepDto> steps;
}
