package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.AsyncTaskResponseDto;
import com.example.recipeplatform.dto.AsyncTaskStatus;
import com.example.recipeplatform.dto.NutritionReportDto;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.RecipeIngredient;
import com.example.recipeplatform.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NutritionCalculatorServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    private Map<UUID, AsyncTaskResponseDto> taskStore;
    private NutritionCalculatorService calculatorService;

    @BeforeEach
    void setUp() {
        taskStore = new ConcurrentHashMap<>();
        calculatorService = new NutritionCalculatorService(recipeRepository, taskStore, 0);
    }

    @Test
    @DisplayName("calculateNutritionAsync should use the ingredient's own stored nutrition values, weighted by quantity")
    void calculateNutritionAsyncShouldUseStoredIngredientValues() throws Exception {
        Ingredient tomato = ingredient(10L, "Tomato", "18", "0.9", "0.2", "3.9", "1");
        Recipe recipe = new Recipe();
        recipe.setId(2L);
        recipe.setTitle("Pasta");
        recipe.setIngredients(Set.of(tomato));
        recipe.replaceRecipeIngredientDetails(List.of(recipeIngredient(tomato, "200", "г")));

        when(recipeRepository.findByIdWithFetchJoin(2L)).thenReturn(Optional.of(recipe));

        UUID taskId = UUID.randomUUID();
        AsyncTaskResponseDto task = new AsyncTaskResponseDto();
        task.setTaskId(taskId);
        task.setStatus(AsyncTaskStatus.IN_PROGRESS);
        taskStore.put(taskId, task);

        NutritionReportDto report = calculatorService.calculateNutritionAsync(2L, taskId).get();

        assertThat(report.getRecipeId()).isEqualTo(2L);
        assertThat(report.getRecipeTitle()).isEqualTo("Pasta");
        assertThat(report.getTotalCaloriesKcal()).isCloseTo(36.0, within(0.01));
        assertThat(report.getTotalProteinsGrams()).isCloseTo(1.8, within(0.01));
        assertThat(report.getIngredients()).hasSize(1);
        assertThat(report.getIngredients().getFirst().getIngredientName()).isEqualTo("Tomato");
        assertThat(report.getIngredients().getFirst().getCaloriesKcal()).isEqualTo(18.0);
        assertThat(report.getIngredients().getFirst().getDataSource())
                .isEqualTo("Ingredient nutrition data stored in the recipe_platform database");
        assertThat(task.getStatus()).isEqualTo(AsyncTaskStatus.COMPLETED);
        assertThat(task.getResult()).isSameAs(report);
    }

    @Test
    @DisplayName("calculateNutritionAsync should convert piece-based units using the ingredient's gramsPerUnit")
    void calculateNutritionAsyncShouldConvertPieceUnits() throws Exception {
        Ingredient egg = ingredient(11L, "Egg", "143", "13", "10", "1", "55");
        Recipe recipe = new Recipe();
        recipe.setId(3L);
        recipe.setTitle("Omelette");
        recipe.setIngredients(Set.of(egg));
        recipe.replaceRecipeIngredientDetails(List.of(recipeIngredient(egg, "2", "шт")));

        when(recipeRepository.findByIdWithFetchJoin(3L)).thenReturn(Optional.of(recipe));

        NutritionReportDto report = calculatorService.calculateNutritionAsync(3L, UUID.randomUUID()).get();

        assertThat(report.getTotalCaloriesKcal()).isCloseTo(157.3, within(0.05));
    }

    @Test
    @DisplayName("calculateNutritionAsync should fall back to the plain ingredient set when no quantity details exist")
    void calculateNutritionAsyncShouldUsePlainIngredientsWhenNoDetails() throws Exception {
        Ingredient tomato = ingredient(10L, "Tomato", "18", "0.9", "0.2", "3.9", "1");
        Recipe recipe = new Recipe();
        recipe.setId(4L);
        recipe.setTitle("Soup");
        recipe.setIngredients(Set.of(tomato));

        when(recipeRepository.findByIdWithFetchJoin(4L)).thenReturn(Optional.of(recipe));

        NutritionReportDto report = calculatorService.calculateNutritionAsync(4L, UUID.randomUUID()).get();

        assertThat(report.getTotalCaloriesKcal()).isEqualTo(18.0);
        assertThat(report.getIngredients().getFirst().getDataSource())
                .isEqualTo("Ingredient nutrition data stored in the recipe_platform database");
    }

    @Test
    @DisplayName("calculateNutritionAsync should wait for the configured minimum progress time")
    void calculateNutritionAsyncShouldWaitForMinimumProgressTime() throws Exception {
        Ingredient tomato = ingredient(10L, "Tomato", "18", "0.9", "0.2", "3.9", "1");
        Recipe recipe = new Recipe();
        recipe.setId(9L);
        recipe.setTitle("Delayed soup");
        recipe.setIngredients(Set.of(tomato));

        when(recipeRepository.findByIdWithFetchJoin(9L)).thenReturn(Optional.of(recipe));

        calculatorService = new NutritionCalculatorService(recipeRepository, taskStore, 100);
        UUID taskId = UUID.randomUUID();
        AsyncTaskResponseDto task = new AsyncTaskResponseDto();
        task.setTaskId(taskId);
        task.setStatus(AsyncTaskStatus.IN_PROGRESS);
        taskStore.put(taskId, task);

        long startedAt = System.nanoTime();
        NutritionReportDto report = calculatorService.calculateNutritionAsync(9L, taskId).get();
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;

        assertThat(report).isNotNull();
        assertThat(elapsedMs).isGreaterThanOrEqualTo(90);
    }

    @Test
    @DisplayName("calculateNutritionAsync should mark task FAILED when unexpected error occurs")
    void calculateNutritionAsyncShouldHandleFailure() {
        when(recipeRepository.findByIdWithFetchJoin(3L)).thenThrow(new RuntimeException("Database error"));

        UUID taskId = UUID.randomUUID();
        AsyncTaskResponseDto task = new AsyncTaskResponseDto();
        task.setTaskId(taskId);
        task.setStatus(AsyncTaskStatus.IN_PROGRESS);
        taskStore.put(taskId, task);

        CompletableFuture<NutritionReportDto> future = calculatorService.calculateNutritionAsync(3L, taskId);

        assertThat(future).isCompletedExceptionally();
        assertThat(task.getStatus()).isEqualTo(AsyncTaskStatus.FAILED);
        assertThat(task.getMessage()).contains("Database error");
    }

    @Test
    @DisplayName("calculateNutritionAsync should fail without a task entry when a recipe is absent")
    void calculateNutritionAsyncShouldHandleFailureWithoutTask() {
        when(recipeRepository.findByIdWithFetchJoin(8L)).thenReturn(Optional.empty());

        CompletableFuture<NutritionReportDto> future = calculatorService.calculateNutritionAsync(8L, UUID.randomUUID());

        assertThat(future).isCompletedExceptionally();
    }

    private Ingredient ingredient(Long id, String name, String calories, String proteins, String fats,
                                  String carbs, String gramsPerUnit) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(id);
        ingredient.setName(name);
        ingredient.setCaloriesPer100g(new BigDecimal(calories));
        ingredient.setProteinsPer100g(new BigDecimal(proteins));
        ingredient.setFatsPer100g(new BigDecimal(fats));
        ingredient.setCarbohydratesPer100g(new BigDecimal(carbs));
        ingredient.setGramsPerUnit(new BigDecimal(gramsPerUnit));
        return ingredient;
    }

    private RecipeIngredient recipeIngredient(Ingredient ingredient, String quantity, String unit) {
        RecipeIngredient detail = new RecipeIngredient();
        detail.setIngredient(ingredient);
        detail.setQuantity(new BigDecimal(quantity));
        detail.setUnit(unit);
        return detail;
    }
}
