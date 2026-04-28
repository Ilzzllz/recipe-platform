package com.example.recipeplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IngredientDto {

    private Long id;

    @NotBlank
    private String name;
}
