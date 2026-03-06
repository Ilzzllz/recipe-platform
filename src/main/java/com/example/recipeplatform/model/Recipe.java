package com.example.recipeplatform.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Recipe {
    private Long id;
    private String title;
    private String description;
    private String secretIngredient; // Это поле мы НЕ будем отдавать в API
}
