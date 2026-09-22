package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.AsyncTaskResponseDto;
import com.example.recipeplatform.dto.AsyncTaskStatus;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NutritionReportServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private NutritionCalculatorService calculatorService;

    private Map<UUID, AsyncTaskResponseDto> taskStore;
    private NutritionReportService nutritionReportService;

    @BeforeEach
    void setUp() {
        taskStore = new ConcurrentHashMap<>();
        nutritionReportService = new NutritionReportService(recipeRepository, calculatorService, taskStore);
    }

    @Test
    @DisplayName("startNutritionReport should create task with IN_PROGRESS status and return valid UUID")
    void startNutritionReportShouldInitializeTask() {
        Recipe recipe = sampleRecipe(1L, "Tomato Soup");
        when(recipeRepository.findByIdWithFetchJoin(1L)).thenReturn(Optional.of(recipe));
        when(calculatorService.calculateNutritionAsync(anyLong(), any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        UUID taskId = nutritionReportService.startNutritionReport(1L);

        assertThat(taskId).isNotNull();
        AsyncTaskResponseDto task = nutritionReportService.getTaskStatus(taskId);
        assertThat(task).isNotNull();
        assertThat(task.getTaskId()).isEqualTo(taskId);
        assertThat(task.getStatus()).isEqualTo(AsyncTaskStatus.IN_PROGRESS);
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
    @DisplayName("getTaskStatus should return a snapshot, not the mutable stored task")
    void getTaskStatusShouldReturnSnapshot() {
        Recipe recipe = sampleRecipe(1L, "Tomato Soup");
        when(recipeRepository.findByIdWithFetchJoin(1L)).thenReturn(Optional.of(recipe));

        UUID taskId = nutritionReportService.startNutritionReport(1L);
        AsyncTaskResponseDto response = nutritionReportService.getTaskStatus(taskId);
        response.setStatus(AsyncTaskStatus.COMPLETED);

        assertThat(nutritionReportService.getTaskStatus(taskId).getStatus())
                .isEqualTo(AsyncTaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("first poll should return IN_PROGRESS even if the calculation has already completed")
    void firstPollShouldReturnInProgressForCompletedTask() {
        Recipe recipe = sampleRecipe(1L, "Tomato Soup");
        when(recipeRepository.findByIdWithFetchJoin(1L)).thenReturn(Optional.of(recipe));

        UUID taskId = nutritionReportService.startNutritionReport(1L);
        taskStore.get(taskId).setStatus(AsyncTaskStatus.COMPLETED);

        assertThat(nutritionReportService.pollTaskStatus(taskId).getStatus())
                .isEqualTo(AsyncTaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("pollTaskStatus should throw NotFoundException when task id is unknown")
    void pollTaskStatusShouldThrowWhenNotFound() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> nutritionReportService.pollTaskStatus(unknownId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Async task with id " + unknownId + " was not found");
    }

    @Test
    @DisplayName("pollTaskStatus should return a normal snapshot for an in-progress task")
    void pollTaskStatusShouldReturnSnapshotForInProgressTask() {
        UUID taskId = UUID.randomUUID();
        AsyncTaskResponseDto task = new AsyncTaskResponseDto();
        task.setTaskId(taskId);
        task.setStatus(AsyncTaskStatus.IN_PROGRESS);
        task.setMessage("Still working");
        taskStore.put(taskId, task);

        AsyncTaskResponseDto response = nutritionReportService.pollTaskStatus(taskId);

        assertThat(response.getStatus()).isEqualTo(AsyncTaskStatus.IN_PROGRESS);
        assertThat(response.getMessage()).isEqualTo("Still working");
        assertThat(response).isNotSameAs(task);
    }

    @Test
    @DisplayName("pollTaskStatus should return the completed snapshot after the first-poll progress window")
    @SuppressWarnings("unchecked")
    void pollTaskStatusShouldReturnCompletedSnapshotAfterProgressWindow() {
        UUID taskId = UUID.randomUUID();
        AsyncTaskResponseDto task = new AsyncTaskResponseDto();
        task.setTaskId(taskId);
        task.setStatus(AsyncTaskStatus.COMPLETED);
        task.setMessage("Done");
        taskStore.put(taskId, task);

        assertThat(nutritionReportService.pollTaskStatus(taskId).getStatus())
                .isEqualTo(AsyncTaskStatus.IN_PROGRESS);
        assertThat(nutritionReportService.pollTaskStatus(taskId).getStatus())
                .isEqualTo(AsyncTaskStatus.IN_PROGRESS);

        Map<UUID, Long> firstPollTimes = (Map<UUID, Long>) ReflectionTestUtils
                .getField(nutritionReportService, "firstPollTimes");
        firstPollTimes.put(taskId, System.nanoTime() - TimeUnit.SECONDS.toNanos(4));

        AsyncTaskResponseDto response = nutritionReportService.pollTaskStatus(taskId);

        assertThat(response.getStatus()).isEqualTo(AsyncTaskStatus.COMPLETED);
        assertThat(response.getMessage()).isEqualTo("Done");
        assertThat(response).isNotSameAs(task);
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
