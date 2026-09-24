package com.example.recipeplatform.mapper;

import com.example.recipeplatform.dto.RecipeCreateDto;
import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.model.CookingStep;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.RecipeIngredient;
import com.example.recipeplatform.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeMapperTest {

    private final RecipeMapper mapper = new RecipeMapper(new IngredientMapper(), new CookingStepMapper());

    @Test
    void toDtoReturnsNullForNullRecipe() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void toDtoUsesLegacyIngredientsWhenDetailsAreEmpty() {
        Recipe recipe = baseRecipe(null);
        Ingredient withNulls = ingredient(1L, null);
        withNulls.setCaloriesPer100g(null);
        withNulls.setProteinsPer100g(null);
        withNulls.setFatsPer100g(null);
        withNulls.setCarbohydratesPer100g(null);
        recipe.replaceIngredients(Set.of(withNulls));
        recipe.setRecipeIngredientDetails(new ArrayList<>());

        CookingStep second = step(2L, 2);
        CookingStep first = step(1L, 1);
        CookingStep duplicateId = step(2L, 3);
        recipe.setSteps(new ArrayList<>(List.of(second, first, duplicateId)));

        RecipeDto dto = mapper.toDto(recipe);

        assertThat(dto.getPortions()).isEqualTo(1);
        assertThat(dto.getAuthor().getUsername()).isEqualTo("anna");
        assertThat(dto.getCategory().getName()).isEqualTo("Супы");
        assertThat(dto.getIngredients()).hasSize(1);
        assertThat(dto.getRecipeIngredients()).singleElement().satisfies(item -> {
            assertThat(item.getQuantity()).isEqualByComparingTo("100");
            assertThat(item.getUnit()).isEqualTo("г");
        });
        assertThat(dto.getSteps()).extracting("stepOrder").containsExactly(1, 2);
        assertThat(dto.getNutrition().getTotalWeightGrams()).isEqualByComparingTo("100");
        assertThat(dto.getNutrition().getCaloriesKcal()).isEqualByComparingTo("0");
    }

    @Test
    void toDtoHandlesNullDetailsAndEmptyRecipeWithoutDivisionByZero() {
        Recipe recipe = baseRecipe(0);
        recipe.setRecipeIngredientDetails(null);

        RecipeDto dto = mapper.toDto(recipe);

        assertThat(dto.getPortions()).isEqualTo(1);
        assertThat(dto.getRecipeIngredients()).isEmpty();
        assertThat(dto.getNutrition().getTotalWeightGrams()).isEqualByComparingTo("0");
        assertThat(dto.getNutrition().getCaloriesPer100g()).isEqualByComparingTo("0");
        assertThat(dto.getNutrition().getPortions()).isEqualTo(1);
    }

    @Test
    void toDtoCalculatesNutritionForEveryUnitKind() {
        Recipe recipe = baseRecipe(2);
        Ingredient egg = ingredient(1L, new BigDecimal("50"));
        Ingredient flour = ingredient(2L, null);
        recipe.replaceIngredients(Set.of(egg, flour));
        recipe.replaceRecipeIngredientDetails(List.of(
                detail(egg, "1", "шт"),
                detail(egg, "1", " ШТ. "),
                detail(egg, "1", "pcs"),
                detail(flour, "100", "г"),
                detail(flour, "50", null),
                detail(flour, null, "мл")));

        RecipeDto dto = mapper.toDto(recipe);

        assertThat(dto.getNutrition().getTotalWeightGrams()).isEqualByComparingTo("300");
        assertThat(dto.getNutrition().getCaloriesKcal()).isEqualByComparingTo("300");
        assertThat(dto.getNutrition().getCaloriesPer100g()).isEqualByComparingTo("100");
        assertThat(dto.getNutrition().getCaloriesPerPortion()).isEqualByComparingTo("150");
        assertThat(dto.getNutrition().getProteinsPerPortion()).isEqualByComparingTo("15");
        assertThat(dto.getNutrition().getFatsPerPortion()).isEqualByComparingTo("15");
        assertThat(dto.getNutrition().getCarbohydratesPerPortion()).isEqualByComparingTo("15");
        assertThat(dto.getPortions()).isEqualTo(2);
        assertThat(dto.getRecipeIngredients()).hasSize(6);
    }

    @Test
    void piecesWithoutGramsPerUnitWeighNothing() {
        Recipe recipe = baseRecipe(1);
        Ingredient noWeight = ingredient(1L, null);
        recipe.replaceRecipeIngredientDetails(List.of(detail(noWeight, "2", "шт")));

        assertThat(mapper.toDto(recipe).getNutrition().getTotalWeightGrams()).isEqualByComparingTo("0");
    }

    @ParameterizedTest
    @CsvSource(value = {"NULL,1", "0,1", "-3,1", "4,4"}, nullValues = "NULL")
    void updateEntityNormalizesTextAndPortions(Integer portions, int expected) {
        RecipeCreateDto request = new RecipeCreateDto();
        request.setTitle("  борщ  ");
        request.setDescription(" наваристый. со сметаной ");
        request.setPortions(portions);

        Recipe recipe = mapper.toEntity(request);

        assertThat(recipe.getTitle()).isEqualTo("Борщ");
        assertThat(recipe.getDescription()).isEqualTo("Наваристый. Со сметаной");
        assertThat(recipe.getPortions()).isEqualTo(expected);
    }

    @Test
    void toDtoListMapsEveryRecipe() {
        assertThat(mapper.toDtoList(List.of(baseRecipe(3), baseRecipe(-1))))
                .extracting(RecipeDto::getPortions)
                .containsExactly(3, 1);
    }

    private Recipe baseRecipe(Integer portions) {
        User author = new User();
        author.setId(1L);
        author.setUsername("anna");
        Category category = new Category();
        category.setId(2L);
        category.setName("Супы");
        Recipe recipe = new Recipe();
        recipe.setId(10L);
        recipe.setTitle("Борщ");
        recipe.setPortions(portions);
        recipe.setAuthor(author);
        recipe.setCategory(category);
        return recipe;
    }

    private Ingredient ingredient(Long id, BigDecimal gramsPerUnit) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(id);
        ingredient.setName("Ingredient " + id);
        ingredient.setCaloriesPer100g(new BigDecimal("100"));
        ingredient.setProteinsPer100g(new BigDecimal("10"));
        ingredient.setFatsPer100g(new BigDecimal("10"));
        ingredient.setCarbohydratesPer100g(new BigDecimal("10"));
        ingredient.setGramsPerUnit(gramsPerUnit);
        return ingredient;
    }

    private RecipeIngredient detail(Ingredient ingredient, String quantity, String unit) {
        RecipeIngredient detail = new RecipeIngredient();
        detail.setIngredient(ingredient);
        detail.setQuantity(quantity == null ? null : new BigDecimal(quantity));
        detail.setUnit(unit);
        return detail;
    }

    private CookingStep step(Long id, int order) {
        CookingStep step = new CookingStep();
        step.setId(id);
        step.setStepOrder(order);
        step.setDescription("Step " + order);
        return step;
    }
}
