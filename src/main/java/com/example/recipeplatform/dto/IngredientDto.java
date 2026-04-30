package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Ingredient response")
public class IngredientDto extends IngredientCreateDto {

    @Schema(description = "Ingredient id", example = "5")
    private Long id;
}
