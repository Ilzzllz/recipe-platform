package com.example.recipeplatform.mapper;

import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.RecipeIngredient;
import com.example.recipeplatform.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeMapperNutritionTest {
    @Test
    void calculatesNutritionFromQuantitiesAndPortions() {
        Ingredient rice = ingredient("Рис", 360, 7, 1, 79);
        Recipe recipe = new Recipe();
        recipe.setTitle("  рис. подать горячим ");
        recipe.setDescription("  гарнир  ");
        recipe.setPortions(2);
        User author = new User(); author.setId(1L); author.setUsername("anna");
        Category category = new Category(); category.setId(1L); category.setName("Гарниры");
        recipe.setAuthor(author); recipe.setCategory(category);
        recipe.replaceIngredients(java.util.Set.of(rice));
        RecipeIngredient detail = new RecipeIngredient(); detail.setIngredient(rice); detail.setQuantity(BigDecimal.valueOf(200)); detail.setUnit("г");
        recipe.replaceRecipeIngredientDetails(List.of(detail));

        RecipeDtoAssert result = new RecipeDtoAssert(new RecipeMapper(new IngredientMapper(), new CookingStepMapper()).toDto(recipe));
        assertThat(result.caloriesPer100g()).isEqualByComparingTo("360");
        assertThat(result.caloriesPerPortion()).isEqualByComparingTo("360");
        assertThat(result.quantity()).isEqualByComparingTo("200");
    }

    private Ingredient ingredient(String name, double calories, double proteins, double fats, double carbs) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L); ingredient.setName(name);
        ingredient.setCaloriesPer100g(BigDecimal.valueOf(calories));
        ingredient.setProteinsPer100g(BigDecimal.valueOf(proteins));
        ingredient.setFatsPer100g(BigDecimal.valueOf(fats));
        ingredient.setCarbohydratesPer100g(BigDecimal.valueOf(carbs));
        ingredient.setGramsPerUnit(BigDecimal.valueOf(50));
        return ingredient;
    }

    private record RecipeDtoAssert(com.example.recipeplatform.dto.RecipeDto dto) {
        BigDecimal caloriesPer100g() { return dto.getNutrition().getCaloriesPer100g(); }
        BigDecimal caloriesPerPortion() { return dto.getNutrition().getCaloriesPerPortion(); }
        BigDecimal quantity() { return dto.getRecipeIngredients().get(0).getQuantity(); }
    }
}
