package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.NutritionReportDto;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.RecipeIngredient;
import com.example.recipeplatform.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NutritionCalculatorServiceBranchesTest {

    @Mock
    private RecipeRepository recipeRepository;

    private NutritionCalculatorService service;

    @BeforeEach
    void setUp() {
        service = new NutritionCalculatorService(recipeRepository, new ConcurrentHashMap<>(), 0);
    }

    @Test
    void nullDetailsFallBackToIngredientsAndNullValuesCountAsZero() throws Exception {
        Ingredient empty = new Ingredient();
        empty.setId(1L);
        empty.setName("Water");
        empty.setCaloriesPer100g(null);
        empty.setProteinsPer100g(null);
        empty.setFatsPer100g(null);
        empty.setCarbohydratesPer100g(null);
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setIngredients(Set.of(empty));
        recipe.setRecipeIngredientDetails(null);
        when(recipeRepository.findByIdWithFetchJoin(1L)).thenReturn(Optional.of(recipe));

        NutritionReportDto report = service.calculateNutritionAsync(1L, UUID.randomUUID()).get();

        assertThat(report.getIngredients()).singleElement()
                .satisfies(item -> assertThat(item.getCaloriesKcal()).isZero());
        assertThat(report.getTotalCaloriesKcal()).isZero();
    }

    @Test
    void quantitiesAreConvertedForEveryUnitKind() throws Exception {
        Ingredient weightless = ingredient(null);
        Ingredient egg = ingredient(new BigDecimal("50"));
        Recipe recipe = new Recipe();
        recipe.setId(2L);
        recipe.setRecipeIngredientDetails(List.of(
                detail(weightless, null, "г"),
                detail(weightless, "2", null),
                detail(weightless, "1", "pcs"),
                detail(egg, "1", "ШТ")));
        when(recipeRepository.findByIdWithFetchJoin(2L)).thenReturn(Optional.of(recipe));

        NutritionReportDto report = service.calculateNutritionAsync(2L, UUID.randomUUID()).get();

        assertThat(report.getTotalCaloriesKcal()).isCloseTo(53.0, within(0.01));
        assertThat(report.getIngredients()).hasSize(4);
    }

    private Ingredient ingredient(BigDecimal gramsPerUnit) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(gramsPerUnit == null ? 10L : 11L);
        ingredient.setName("Ingredient");
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
}
