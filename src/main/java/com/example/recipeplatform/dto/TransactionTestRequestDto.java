package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request payload for transaction demo scenarios")
public class TransactionTestRequestDto {

    @NotBlank
    @Schema(description = "Username prefix that will be extended with a unique marker", example = "demo_user")
    private String userUsername;

    @NotBlank
    @Email
    @Schema(description = "Base email for the demo user", example = "demo@example.com")
    private String userEmail;

    @Schema(description = "Optional bio for the demo user", example = "Created for transaction demos")
    private String userBio;

    @NotBlank
    @Schema(description = "Category name prefix", example = "demo_category")
    private String categoryName;

    @Schema(description = "Optional category description", example = "Temporary category for transaction checks")
    private String categoryDescription;

    @NotBlank
    @Schema(description = "Ingredient name prefix", example = "demo_ingredient")
    private String ingredientName;

    @NotBlank
    @Schema(description = "Recipe title prefix", example = "demo_recipe")
    private String recipeTitle;

    @NotBlank
    @Schema(description = "Recipe description used in the transactional demo", example = "Will this recipe be rolled back?")
    private String recipeDescription;
}