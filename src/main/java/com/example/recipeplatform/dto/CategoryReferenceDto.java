package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Minimal category reference inside recipe response")
public class CategoryReferenceDto {

    @Schema(description = "Category id", example = "2")
    private Long id;

    @Schema(description = "Category name", example = "Soups")
    private String name;
}