package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.AsyncTaskResponseDto;
import com.example.recipeplatform.dto.AsyncTaskStatus;
import com.example.recipeplatform.dto.NutritionReportDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.repository.RecipeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NutritionReportServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private ObjectMapper objectMapper;

    private NutritionReportService nutritionReportService;

    @BeforeEach
    void setUp() {
        nutritionReportService = new NutritionReportService(recipeRepository, objectMapper);
    }

    @Test
    @DisplayName("startNutritionReport should create task with IN_PROGRESS status and return valid UUID")
    void startNutritionReportShouldInitializeTask() {
        Recipe recipe = sampleRecipe(1L, "Tomato Soup");
        when(recipeRepository.findByIdWithFetchJoin(1L)).thenReturn(Optional.of(recipe));

        UUID taskId = nutritionReportService.startNutritionReport(1L);

        assertThat(taskId).isNotNull();
        AsyncTaskResponseDto task = nutritionReportService.getTaskStatus(taskId);
        assertThat(task).isNotNull();
        assertThat(task.getTaskId()).isEqualTo(taskId);
    }

    @Test
    @DisplayName("startNutritionReport should throw NotFoundException when recipe does not exist")
    void startNutritionReportShouldThrowWhenRecipeNotFound() {
        when(recipeRepository.findByIdWithFetchJoin(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> nutritionReportService.startNutritionReport(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Recipe with id 999 was not found");
    }

    @Test
    @DisplayName("getTaskStatus should throw NotFoundException when task id is unknown")
    void getTaskStatusShouldThrowWhenNotFound() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> nutritionReportService.getTaskStatus(unknownId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Async task with id " + unknownId + " was not found");
    }

    @Test
    @DisplayName("calculateNutritionAsync should aggregate nutrition values and mark task COMPLETED")
    void calculateNutritionAsyncShouldCompleteSuccessfully() throws Exception {
        Recipe recipe = sampleRecipe(2L, "Pasta");
        when(recipeRepository.findByIdWithFetchJoin(2L)).thenReturn(Optional.of(recipe));

        UUID taskId = UUID.randomUUID();
        AsyncTaskResponseDto task = new AsyncTaskResponseDto();
        task.setTaskId(taskId);
        task.setStatus(AsyncTaskStatus.IN_PROGRESS);
        nutritionReportService.getTaskStore().put(taskId, task);

        CompletableFuture<NutritionReportDto> future = nutritionReportService.calculateNutritionAsync(2L, taskId);
        NutritionReportDto report = future.get();

        assertThat(report).isNotNull();
        assertThat(report.getRecipeId()).isEqualTo(2L);
        assertThat(report.getRecipeTitle()).isEqualTo("Pasta");
        assertThat(report.getTotalCaloriesKcal()).isGreaterThan(0.0);
        assertThat(report.getIngredients()).hasSize(1);
        assertThat(task.getStatus()).isEqualTo(AsyncTaskStatus.COMPLETED);
        assertThat(task.getResult()).isSameAs(report);
    }

    @Test
    @DisplayName("calculateNutritionAsync should mark task FAILED when unexpected error occurs")
    void calculateNutritionAsyncShouldHandleFailure() {
        when(recipeRepository.findByIdWithFetchJoin(3L)).thenThrow(new RuntimeException("Database error"));

        UUID taskId = UUID.randomUUID();
        AsyncTaskResponseDto task = new AsyncTaskResponseDto();
        task.setTaskId(taskId);
        task.setStatus(AsyncTaskStatus.IN_PROGRESS);
        nutritionReportService.getTaskStore().put(taskId, task);

        CompletableFuture<NutritionReportDto> future = nutritionReportService.calculateNutritionAsync(3L, taskId);

        assertThat(future).isCompletedExceptionally();
        assertThat(task.getStatus()).isEqualTo(AsyncTaskStatus.FAILED);
        assertThat(task.getMessage()).contains("Database error");
    }

    private Recipe sampleRecipe(Long id, String title) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setTitle(title);

        Ingredient ingredient = new Ingredient();
        ingredient.setId(10L);
        ingredient.setName("Tomato");
        recipe.setIngredients(Set.of(ingredient));

        return recipe;
    }
}
