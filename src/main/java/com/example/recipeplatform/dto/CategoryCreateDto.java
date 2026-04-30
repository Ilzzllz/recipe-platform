package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Category create or update request")
public class CategoryCreateDto {

    @NotBlank
    @Schema(description = "Category name", example = "Soups")
    private String name;

    @Schema(description = "Category description", example = "Hot soups for everyday cooking")
    private String description;
}
