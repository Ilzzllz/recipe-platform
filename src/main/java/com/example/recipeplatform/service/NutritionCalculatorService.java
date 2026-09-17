package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.AsyncTaskResponseDto;
import com.example.recipeplatform.dto.AsyncTaskStatus;
import com.example.recipeplatform.dto.IngredientNutritionDto;
import com.example.recipeplatform.dto.NutritionReportDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.repository.RecipeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NutritionCalculatorService {

    private static final Logger logger = LoggerFactory.getLogger(NutritionCalculatorService.class);
    private static final String OPEN_FOOD_FACTS_SEARCH_URL =
            "https://world.openfoodfacts.org/cgi/search.pl?search_terms={query}&search_simple=1&action=process&json=1&page_size=1";

    private final RecipeRepository recipeRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Map<UUID, AsyncTaskResponseDto> taskStore;

    public NutritionCalculatorService(RecipeRepository recipeRepository,
                                      ObjectMapper objectMapper,
                                      Map<UUID, AsyncTaskResponseDto> taskStore,
                                      RestClient restClient) {
        this.recipeRepository = recipeRepository;
        this.objectMapper = objectMapper;
        this.taskStore = taskStore;
        this.restClient = restClient;
    }

    @Async("recipeTaskExecutor")
    public CompletableFuture<NutritionReportDto> calculateNutritionAsync(Long recipeId, UUID taskId) {
        AsyncTaskResponseDto task = taskStore.get(taskId);
        try {
            Recipe recipe = recipeRepository.findByIdWithFetchJoin(recipeId)
                    .orElseThrow(() -> new NotFoundException("Recipe with id " + recipeId + " was not found"));

            List<IngredientNutritionDto> ingredientNutritions = new ArrayList<>();
            double totalCalories = 0.0, totalProteins = 0.0, totalFats = 0.0, totalCarbs = 0.0;

            for (Ingredient ingredient : recipe.getIngredients()) {
                IngredientNutritionDto nut = fetchIngredientNutrition(ingredient.getName());
                ingredientNutritions.add(nut);
                totalCalories += nut.getCaloriesKcal();
                totalProteins += nut.getProteinsGrams();
                totalFats += nut.getFatsGrams();
                totalCarbs += nut.getCarbohydratesGrams();
            }

            NutritionReportDto report = new NutritionReportDto();
            report.setRecipeId(recipe.getId());
            report.setRecipeTitle(recipe.getTitle());
            report.setTotalCaloriesKcal(Math.round(totalCalories * 10.0) / 10.0);
            report.setTotalProteinsGrams(Math.round(totalProteins * 10.0) / 10.0);
            report.setTotalFatsGrams(Math.round(totalFats * 10.0) / 10.0);
            report.setTotalCarbohydratesGrams(Math.round(totalCarbs * 10.0) / 10.0);
            report.setIngredients(ingredientNutritions);
            report.setCalculatedAt(LocalDateTime.now());

            if (task != null) {
                task.setStatus(AsyncTaskStatus.COMPLETED);
                task.setCompletedAt(LocalDateTime.now());
                task.setMessage("Nutritional report calculated successfully.");
                task.setResult(report);
            }
            return CompletableFuture.completedFuture(report);
        } catch (Exception e) {
            logger.error("Failed to compute nutrition report for task {}: {}", taskId, e.getMessage());
            if (task != null) {
                task.setStatus(AsyncTaskStatus.FAILED);
                task.setCompletedAt(LocalDateTime.now());
                task.setMessage("Failed to compute nutrition report: " + e.getMessage());
            }
            CompletableFuture<NutritionReportDto> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    private IngredientNutritionDto fetchIngredientNutrition(String ingredientName) {
        try {
            String encoded = URLEncoder.encode(ingredientName, StandardCharsets.UTF_8);
            String rawJson = restClient.get()
                    .uri(OPEN_FOOD_FACTS_SEARCH_URL, encoded)
                    .retrieve()
                    .body(String.class);

            if (rawJson != null && !rawJson.isBlank()) {
                JsonNode root = objectMapper.readTree(rawJson);
                JsonNode products = root.path("products");
                if (products.isArray() && !products.isEmpty()) {
                    JsonNode nutriments = products.get(0).path("nutriments");
                    double kcal = nutriments.path("energy-kcal_100g").asDouble(nutriments.path("energy-kcal").asDouble(50.0));
                    double proteins = nutriments.path("proteins_100g").asDouble(nutriments.path("proteins").asDouble(2.0));
                    double fat = nutriments.path("fat_100g").asDouble(nutriments.path("fat").asDouble(1.0));
                    double carbs = nutriments.path("carbohydrates_100g").asDouble(nutriments.path("carbohydrates").asDouble(8.0));
                    return new IngredientNutritionDto(ingredientName, kcal, proteins, fat, carbs, "Open Food Facts API");
                }
            }
        } catch (Exception ex) {
            logger.warn("Could not query Open Food Facts API for '{}': {}", ingredientName, ex.getMessage());
        }
        return new IngredientNutritionDto(ingredientName, 45.0, 1.5, 0.5, 9.0, "Standard culinary estimate");
    }
}