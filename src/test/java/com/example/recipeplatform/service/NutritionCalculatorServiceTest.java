package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.AsyncTaskResponseDto;
import com.example.recipeplatform.dto.AsyncTaskStatus;
import com.example.recipeplatform.dto.NutritionReportDto;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class NutritionCalculatorServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    private Map<UUID, AsyncTaskResponseDto> taskStore;
    private NutritionCalculatorService calculatorService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        taskStore = new ConcurrentHashMap<>();
        RestClient.Builder restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.build();
        calculatorService = new NutritionCalculatorService(
                recipeRepository, new ObjectMapper(), taskStore, restClient, 0);
    }

    @Test
    @DisplayName("calculateNutritionAsync should use API data when Open Food Facts responds successfully")
    void calculateNutritionAsyncShouldCompleteSuccessfully() throws Exception {
        String apiJson = """
                {
                  "products": [
                    {
                      "nutriments": {
                        "energy-kcal_100g": 100.0,
                        "proteins_100g": 5.0,
                        "fat_100g": 2.0,
                        "carbohydrates_100g": 15.0
                      }
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo(containsString("search.pl")))
                .andRespond(withSuccess(apiJson, MediaType.APPLICATION_JSON));

        Recipe recipe = sampleRecipe(2L, "Pasta");
        when(recipeRepository.findByIdWithFetchJoin(2L)).thenReturn(Optional.of(recipe));

        UUID taskId = UUID.randomUUID();
        AsyncTaskResponseDto task = new AsyncTaskResponseDto();
        task.setTaskId(taskId);
        task.setStatus(AsyncTaskStatus.IN_PROGRESS);
        taskStore.put(taskId, task);

        CompletableFuture<NutritionReportDto> future = calculatorService.calculateNutritionAsync(2L, taskId);
        NutritionReportDto report = future.get();

        assertThat(report).isNotNull();
        assertThat(report.getRecipeId()).isEqualTo(2L);
        assertThat(report.getRecipeTitle()).isEqualTo("Pasta");
        assertThat(report.getTotalCaloriesKcal()).isEqualTo(100.0);
        assertThat(report.getIngredients()).hasSize(1);
        assertThat(report.getIngredients().getFirst().getDataSource()).isEqualTo("Open Food Facts API");
        assertThat(task.getStatus()).isEqualTo(AsyncTaskStatus.COMPLETED);
        assertThat(task.getResult()).isSameAs(report);
        mockServer.verify();
    }

    @Test
    @DisplayName("calculateNutritionAsync should fall back to culinary estimate when API fails")
    void calculateNutritionAsyncShouldFallbackWhenApiFails() throws Exception {
        mockServer.expect(requestTo(containsString("search.pl")))
                .andRespond(withServerError());

        Recipe recipe = sampleRecipe(4L, "Soup");
        when(recipeRepository.findByIdWithFetchJoin(4L)).thenReturn(Optional.of(recipe));

        UUID taskId = UUID.randomUUID();
        AsyncTaskResponseDto task = new AsyncTaskResponseDto();
        task.setTaskId(taskId);
        task.setStatus(AsyncTaskStatus.IN_PROGRESS);
        taskStore.put(taskId, task);

        NutritionReportDto report = calculatorService.calculateNutritionAsync(4L, taskId).get();

        assertThat(report.getIngredients()).hasSize(1);
        assertThat(report.getIngredients().getFirst().getDataSource()).isEqualTo("Standard culinary estimate");
        assertThat(task.getStatus()).isEqualTo(AsyncTaskStatus.COMPLETED);
        mockServer.verify();
    }

    @Test
    @DisplayName("calculateNutritionAsync should use a fallback for blank and empty API responses")
    void calculateNutritionAsyncShouldFallbackForBlankAndEmptyApiResponses() throws Exception {
        mockServer.expect(requestTo(containsString("search.pl")))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        Recipe recipe = sampleRecipe(5L, "Blank response soup");
        when(recipeRepository.findByIdWithFetchJoin(5L)).thenReturn(Optional.of(recipe));

        NutritionReportDto report = calculatorService.calculateNutritionAsync(5L, UUID.randomUUID()).get();

        assertThat(report.getIngredients().getFirst().getDataSource()).isEqualTo("Standard culinary estimate");
        mockServer.verify();
    }

    @Test
    @DisplayName("calculateNutritionAsync should use a fallback when products are empty")
    void calculateNutritionAsyncShouldFallbackForEmptyProducts() throws Exception {
        mockServer.expect(requestTo(containsString("search.pl")))
                .andRespond(withSuccess("{\"products\": []}", MediaType.APPLICATION_JSON));

        Recipe recipe = sampleRecipe(6L, "Empty products soup");
        when(recipeRepository.findByIdWithFetchJoin(6L)).thenReturn(Optional.of(recipe));

        NutritionReportDto report = calculatorService.calculateNutritionAsync(6L, UUID.randomUUID()).get();

        assertThat(report.getIngredients().getFirst().getDataSource()).isEqualTo("Standard culinary estimate");
        mockServer.verify();
    }

    @Test
    @DisplayName("calculateNutritionAsync should use a fallback when products are not an array")
    void calculateNutritionAsyncShouldFallbackForNonArrayProducts() throws Exception {
        mockServer.expect(requestTo(containsString("search.pl")))
                .andRespond(withSuccess("{\"products\": {}}", MediaType.APPLICATION_JSON));

        Recipe recipe = sampleRecipe(7L, "Invalid products soup");
        when(recipeRepository.findByIdWithFetchJoin(7L)).thenReturn(Optional.of(recipe));

        NutritionReportDto report = calculatorService.calculateNutritionAsync(7L, UUID.randomUUID()).get();

        assertThat(report.getIngredients().getFirst().getDataSource()).isEqualTo("Standard culinary estimate");
        mockServer.verify();
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
