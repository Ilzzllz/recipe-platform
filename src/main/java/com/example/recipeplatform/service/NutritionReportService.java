package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.AsyncTaskResponseDto;
import com.example.recipeplatform.dto.AsyncTaskStatus;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class NutritionReportService {

    private static final long FIRST_POLL_PROGRESS_WINDOW_NANOS = TimeUnit.SECONDS.toNanos(3);

    private final RecipeRepository recipeRepository;
    private final NutritionCalculatorService calculatorService;
    private final Map<UUID, AsyncTaskResponseDto> taskStore;
    private final Map<UUID, Long> firstPollTimes = new ConcurrentHashMap<>();

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
        task.setStartedAt(LocalDateTime.now(Clock.systemDefaultZone()));
        task.setMessage("Calculating nutrition from stored ingredient data for "
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
        return snapshot(task);
    }

    public AsyncTaskResponseDto pollTaskStatus(UUID taskId) {
        AsyncTaskResponseDto task = taskStore.get(taskId);
        if (task == null) {
            throw new NotFoundException("Async task with id " + taskId + " was not found");
        }

        long now = System.nanoTime();
        Long firstPollAt = firstPollTimes.putIfAbsent(taskId, now);
        if (task.getStatus() == AsyncTaskStatus.COMPLETED
                && (firstPollAt == null || now - firstPollAt < FIRST_POLL_PROGRESS_WINDOW_NANOS)) {
            return inProgressSnapshot(task);
        }
        return snapshot(task);
    }

    private AsyncTaskResponseDto snapshot(AsyncTaskResponseDto task) {
        AsyncTaskResponseDto response = new AsyncTaskResponseDto();
        response.setTaskId(task.getTaskId());
        response.setStatus(task.getStatus());
        response.setStartedAt(task.getStartedAt());
        response.setCompletedAt(task.getCompletedAt());
        response.setMessage(task.getMessage());
        response.setResult(task.getResult());
        return response;
    }

    private AsyncTaskResponseDto inProgressSnapshot(AsyncTaskResponseDto task) {
        AsyncTaskResponseDto response = snapshot(task);
        response.setStatus(AsyncTaskStatus.IN_PROGRESS);
        response.setCompletedAt(null);
        response.setMessage("Nutritional report is still being processed.");
        response.setResult(null);
        return response;
    }
}
