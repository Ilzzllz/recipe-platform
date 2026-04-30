package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Cooking step used inside recipe create and update requests")
public class RecipeStepCreateDto {

    @NotNull
    @Schema(description = "Step order inside the recipe", example = "1")
    private Integer stepOrder;

    @NotBlank
    @Schema(description = "Step description", example = "Boil the pasta until al dente")
    private String description;
}
