package com.example.recipeplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IngredientCreateDto {

    @NotBlank
    private String name;
}
