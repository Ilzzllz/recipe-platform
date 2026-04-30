package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Ingredient create or update request")
public class IngredientCreateDto {

    @NotBlank
    @Schema(description = "Ingredient name", example = "Olive oil")
    private String name;
}
