package com.example.recipeplatform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class RecipeRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Long authorId;

    @NotNull
    private Long categoryId;

    @NotEmpty
    private Set<Long> ingredientIds;

    @Valid
    @NotEmpty
    private List<StepRequest> steps;
}
