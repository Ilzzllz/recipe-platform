package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.AsyncTaskResponseDto;
import com.example.recipeplatform.dto.AsyncTaskStatus;
import com.example.recipeplatform.dto.IngredientNutritionDto;
import com.example.recipeplatform.dto.NutritionReportDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.RecipeIngredient;
import com.example.recipeplatform.repository.RecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class NutritionCalculatorService {

    private static final Logger logger = LoggerFactory.getLogger(NutritionCalculatorService.class);
    private static final String INTERNAL_DATA_SOURCE = "Ingredient nutrition data stored in the recipe_platform database";

    private final RecipeRepository recipeRepository;
    private final Map<UUID, AsyncTaskResponseDto> taskStore;
    private final long initialDelayMs;

    public NutritionCalculatorService(RecipeRepository recipeRepository,
                                      Map<UUID, AsyncTaskResponseDto> taskStore,
                                      @Value("${app.async.nutrition-initial-delay-ms:10000}") long initialDelayMs) {
        this.recipeRepository = recipeRepository;
        this.taskStore = taskStore;
        this.initialDelayMs = initialDelayMs;
    }

    @Async("recipeTaskExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<NutritionReportDto> calculateNutritionAsync(Long recipeId, UUID taskId) {
        AsyncTaskResponseDto task = taskStore.get(taskId);
        long calculationStartedAt = System.nanoTime();
        try {
            Recipe recipe = recipeRepository.findByIdWithFetchJoin(recipeId)
                    .orElseThrow(() -> new NotFoundException("Recipe with id " + recipeId + " was not found"));

            List<IngredientNutritionDto> ingredientNutritions = new ArrayList<>();
            double totalCalories = 0.0;
            double totalProteins = 0.0;
            double totalFats = 0.0;
            double totalCarbs = 0.0;

            if (recipe.getRecipeIngredientDetails() != null && !recipe.getRecipeIngredientDetails().isEmpty()) {
                for (RecipeIngredient detail : recipe.getRecipeIngredientDetails()) {
                    double factor = grams(detail.getQuantity(), detail.getUnit(), detail.getIngredient().getGramsPerUnit()) / 100.0;
                    IngredientNutritionDto nut = toIngredientNutrition(detail.getIngredient());
                    ingredientNutritions.add(nut);
                    totalCalories += nut.getCaloriesKcal() * factor;
                    totalProteins += nut.getProteinsGrams() * factor;
                    totalFats += nut.getFatsGrams() * factor;
                    totalCarbs += nut.getCarbohydratesGrams() * factor;
                }
            } else {
                for (Ingredient ingredient : recipe.getIngredients()) {
                    IngredientNutritionDto nut = toIngredientNutrition(ingredient);
                    ingredientNutritions.add(nut);
                    totalCalories += nut.getCaloriesKcal();
                    totalProteins += nut.getProteinsGrams();
                    totalFats += nut.getFatsGrams();
                    totalCarbs += nut.getCarbohydratesGrams();
                }
            }

            NutritionReportDto report = new NutritionReportDto();
            report.setRecipeId(recipe.getId());
            report.setRecipeTitle(recipe.getTitle());
            report.setTotalCaloriesKcal(Math.round(totalCalories * 10.0) / 10.0);
            report.setTotalProteinsGrams(Math.round(totalProteins * 10.0) / 10.0);
            report.setTotalFatsGrams(Math.round(totalFats * 10.0) / 10.0);
            report.setTotalCarbohydratesGrams(Math.round(totalCarbs * 10.0) / 10.0);
            report.setIngredients(ingredientNutritions);
            report.setCalculatedAt(LocalDateTime.now(Clock.systemDefaultZone()));

            if (task != null) {
                waitUntilMinimumProgressTimeHasElapsed(calculationStartedAt);
                task.setCompletedAt(LocalDateTime.now(Clock.systemDefaultZone()));
                task.setMessage("Nutritional report calculated successfully.");
                task.setResult(report);
                task.setStatus(AsyncTaskStatus.COMPLETED);
            }
            return CompletableFuture.completedFuture(report);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return failedTask(taskId, task, e);
        } catch (Exception e) {
            return failedTask(taskId, task, e);
        }
    }

    private CompletableFuture<NutritionReportDto> failedTask(UUID taskId,
                                                             AsyncTaskResponseDto task,
                                                             Exception exception) {
        logger.error("Failed to compute nutrition report for task {}: {}", taskId, exception.getMessage());
        if (task != null) {
            task.setStatus(AsyncTaskStatus.FAILED);
            task.setCompletedAt(LocalDateTime.now(Clock.systemDefaultZone()));
            task.setMessage("Failed to compute nutrition report: " + exception.getMessage());
        }
        CompletableFuture<NutritionReportDto> failed = new CompletableFuture<>();
        failed.completeExceptionally(exception);
        return failed;
    }

    private void waitUntilMinimumProgressTimeHasElapsed(long calculationStartedAt) throws InterruptedException {
        long elapsedMs = (System.nanoTime() - calculationStartedAt) / 1_000_000;
        long remainingMs = initialDelayMs - elapsedMs;
        if (remainingMs > 0) {
            Thread.sleep(remainingMs);
        }
    }

    private IngredientNutritionDto toIngredientNutrition(Ingredient ingredient) {
        return new IngredientNutritionDto(
                ingredient.getName(),
                value(ingredient.getCaloriesPer100g()),
                value(ingredient.getProteinsPer100g()),
                value(ingredient.getFatsPer100g()),
                value(ingredient.getCarbohydratesPer100g()),
                INTERNAL_DATA_SOURCE);
    }

    private double value(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private double grams(BigDecimal quantity, String unit, BigDecimal gramsPerUnit) {
        double value = quantity == null ? 0 : quantity.doubleValue();
        if (unit != null && (unit.equalsIgnoreCase("шт") || unit.equalsIgnoreCase("pcs"))) {
            return value * (gramsPerUnit == null ? 1 : gramsPerUnit.doubleValue());
        }
        return value;
    }
}
