package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Recipe create or full update request")
public class RecipeCreateDto {

    @NotBlank
    @Schema(description = "Recipe title", example = "Pumpkin Cream Soup")
    private String title;

    @NotBlank
    @Schema(description = "Short recipe description", example = "Smooth autumn soup with pumpkin and cream")
    private String description;

    @NotNull
    @Schema(description = "Existing author id", example = "1")
    private Long authorId;

    @NotNull
    @Schema(description = "Existing category id", example = "2")
    private Long categoryId;

    @NotEmpty
    @ArraySchema(schema = @Schema(description = "Existing ingredient id", example = "3"))
    private Set<Long> ingredientIds;

    @Valid
    @NotEmpty
    @Schema(description = "Recipe steps. recipeId is not needed here because the recipe is created or updated as one object.")
    private List<RecipeStepCreateDto> steps;
}
