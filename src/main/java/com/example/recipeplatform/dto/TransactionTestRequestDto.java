package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Запрос для демонстрации транзакций (частичное сохранение / rollback)")
public class TransactionTestRequestDto {

    @NotBlank
    @Schema(description = "Имя пользователя (будет дополнено уникальным маркером)", example = "demo_user")
    private String userUsername;

    @NotBlank
    @Schema(description = "Email пользователя", example = "demo@example.com")
    private String userEmail;

    @Schema(description = "Bio пользователя", example = "Создан для демонстрации транзакций")
    private String userBio;

    @NotBlank
    @Schema(description = "Название категории", example = "demo_category")
    private String categoryName;

    @Schema(description = "Описание категории", example = "Временная категория")
    private String categoryDescription;

    @NotBlank
    @Schema(description = "Название ингредиента", example = "demo_ingredient")
    private String ingredientName;

    @NotBlank
    @Schema(description = "Заголовок рецепта", example = "demo_recipe")
    private String recipeTitle;

    @Schema(description = "Описание рецепта", example = "Будет ли сохранён?")
    private String recipeDescription;
}