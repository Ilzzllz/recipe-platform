package com.example.recipeplatform.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecipeDto {

    private Long id;
    private String title;
    private String description;
    private Long authorId;
    private String authorUsername;
    private Long categoryId;
    private String categoryName;
    private List<IngredientDto> ingredients;
    private List<CookingStepDto> steps;
}
