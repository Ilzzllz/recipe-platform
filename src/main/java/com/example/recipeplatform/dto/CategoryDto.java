package com.example.recipeplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDto {

    private Long id;

    @NotBlank
    private String name;

    private String description;
}
