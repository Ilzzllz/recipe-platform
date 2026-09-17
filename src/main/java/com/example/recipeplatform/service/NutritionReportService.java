package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.AsyncTaskResponseDto;
import com.example.recipeplatform.dto.AsyncTaskStatus;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class NutritionReportService {

    private final RecipeRepository recipeRepository;
    private final NutritionCalculatorService calculatorService;
    private final Map<UUID, AsyncTaskResponseDto> taskStore;

    public NutritionReportService(RecipeRepository recipeRepository,
                                  NutritionCalculatorService calculatorService,
                                  Map<UUID, AsyncTaskResponseDto> taskStore) {
        this.recipeRepository = recipeRepository;
        this.calculatorService = calculatorService;
        this.taskStore = taskStore;
    }

    public UUID startNutritionReport(Long recipeId) {
        var recipe = recipeRepository.findByIdWithFetchJoin(recipeId)
                .orElseThrow(() -> new NotFoundException("Recipe with id " + recipeId + " was not found"));

        UUID taskId = UUID.randomUUID();
        AsyncTaskResponseDto task = new AsyncTaskResponseDto();
        task.setTaskId(taskId);
        task.setStatus(AsyncTaskStatus.IN_PROGRESS);
        task.setStartedAt(LocalDateTime.now());
        task.setMessage("Fetching nutrition data from Open Food Facts API for "
                + recipe.getIngredients().size() + " ingredients...");
        taskStore.put(taskId, task);

        calculatorService.calculateNutritionAsync(recipeId, taskId);

        return taskId;
    }

    public AsyncTaskResponseDto getTaskStatus(UUID taskId) {
        AsyncTaskResponseDto task = taskStore.get(taskId);
        if (task == null) {
            throw new NotFoundException("Async task with id " + taskId + " was not found");
        }
        return task;
    }
}