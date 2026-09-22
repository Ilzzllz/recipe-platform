package com.example.recipeplatform.controller;

import com.example.recipeplatform.dto.AsyncTaskResponseDto;
import com.example.recipeplatform.dto.CounterStatsDto;
import com.example.recipeplatform.dto.NPlusOneDemoResponse;
import com.example.recipeplatform.dto.RaceConditionDemoResultDto;
import com.example.recipeplatform.dto.RecipeCreateDto;
import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.dto.RecipeFilterDto;
import com.example.recipeplatform.exception.ApiError;
import com.example.recipeplatform.service.NutritionReportService;
import com.example.recipeplatform.service.RecipeService;
import com.example.recipeplatform.service.RecipeViewCounterService;
import org.springframework.http.ResponseEntity;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/recipes")
@Tag(name = "Recipes", description = "CRUD operations and lab demos for recipes")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Validation error in recipe data",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Recipe or referenced entity was not found",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "Database constraint conflict",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
})
public class RecipeController {

    private static final String TRANSACTIONAL_BULK_EXAMPLE = """
            [
              {
                "title": "TX valid recipe 1",
                "description": "The first valid recipe",
                "authorId": 1,
                "categoryId": 2,
                "ingredientIds": [3],
                "steps": [{ "stepOrder": 1, "description": "Prepare ingredients" }]
              },
              {
                "title": "TX broken recipe 2",
                "description": "The second recipe contains an invalid ingredient",
                "authorId": 1,
                "categoryId": 2,
                "ingredientIds": [999999],
                "steps": [{ "stepOrder": 1, "description": "This recipe must fail" }]
              }
            ]
            """;

    private final RecipeService recipeService;
    private final RecipeViewCounterService recipeViewCounterService;
    private final NutritionReportService nutritionReportService;

    public RecipeController(RecipeService recipeService,
                            RecipeViewCounterService recipeViewCounterService,
                            NutritionReportService nutritionReportService) {
        this.recipeService = recipeService;
        this.recipeViewCounterService = recipeViewCounterService;
        this.nutritionReportService = nutritionReportService;
    }

    @GetMapping
    @Operation(summary = "Get all recipes")
    public List<RecipeDto> getAll() {
        return recipeService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recipe by id", description = "Retrieves recipe details and increments view counters (safe and unsafe) for concurrency testing.")
    public RecipeDto getById(@Parameter(description = "Existing recipe id", example = "12")
                             @PathVariable Long id) {
        recipeViewCounterService.recordView();
        return recipeService.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search recipes by title")
    public List<RecipeDto> getByTitle(@Parameter(description = "Part of the recipe title", example = "soup")
                                      @RequestParam @NotBlank(message = "Title cannot be blank") String title) {
        return recipeService.searchByTitle(title);
    }

    @GetMapping("/n-plus-one/problem")
    @Operation(summary = "Demonstrate the N+1 problem",
            description = "Loads recipes without fetch join so the SQL statement count is higher.")
    public NPlusOneDemoResponse showNPlusOneProblem() {
        return recipeService.demonstrateNPlusOneProblem();
    }

    @GetMapping("/n-plus-one/solution")
    @Operation(summary = "Show the fetch join solution for N+1",
            description = "Loads recipes with fetch join so the SQL statement count is lower.")
    public NPlusOneDemoResponse showNPlusOneSolution() {
        return recipeService.demonstrateNPlusOneSolution();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new recipe")
    public RecipeDto create(@Valid @RequestBody RecipeCreateDto request) {
        return recipeService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing recipe",
            description = "PUT performs a full replacement of recipe fields, ingredient ids and the entire steps list.")
    public RecipeDto update(@Parameter(description = "Existing recipe id", example = "12")
                            @PathVariable Long id,
                            @Valid @RequestBody RecipeCreateDto request) {
        return recipeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete recipe by id")
    @ApiResponse(responseCode = "204", description = "Recipe deleted successfully")
    public void delete(@Parameter(description = "Existing recipe id", example = "12")
                       @PathVariable Long id) {
        recipeService.delete(id);
    }

    @GetMapping("/filter/jpql")
    @Operation(summary = "Filter recipes by nested author and category fields (JPQL + pagination)",
            description = "Filters by Recipe.author.username and Recipe.category.name. Uses a JPQL projection, pagination and in-memory caching without lazy collection loading.")
    public Page<RecipeFilterDto> filterByAuthorAndCategoryJPQL(
            @Parameter(description = "Author username (case-insensitive)", example = "anna")
            @RequestParam @NotBlank(message = "Author username cannot be blank") String authorUsername,
            @Parameter(description = "Category name (case-insensitive)", example = "Soups")
            @RequestParam @NotBlank(message = "Category name cannot be blank") String categoryName,
            @ParameterObject @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return recipeService.findByAuthorAndCategoryJPQL(authorUsername, categoryName, pageable);
    }

    @GetMapping("/filter/native")
    @Operation(summary = "Filter recipes by nested author and category fields (native SQL + pagination)",
            description = "Native SQL analogue of the JPQL filter with users and categories JOINs, pagination and in-memory caching.")
    public Page<RecipeFilterDto> filterByAuthorAndCategoryNative(
            @Parameter(description = "Author username (case-insensitive)", example = "anna")
            @RequestParam @NotBlank(message = "Author username cannot be blank") String authorUsername,
            @Parameter(description = "Category name (case-insensitive)", example = "Soups")
            @RequestParam @NotBlank(message = "Category name cannot be blank") String categoryName,
            @ParameterObject @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return recipeService.findByAuthorAndCategoryNative(authorUsername, categoryName, pageable);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Bulk create recipes with transaction",
            description = "Creates multiple recipes atomically. If any recipe fails, none are saved.")
    public List<RecipeDto> createBulk(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Two recipes: the second contains an intentionally invalid ingredient id.",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Rollback demonstration",
                                    value = TRANSACTIONAL_BULK_EXAMPLE)))
            @RequestBody @NotEmpty List<@Valid RecipeCreateDto> dtos) {
        return recipeService.createBulk(dtos);
    }

    @PostMapping("/bulk/no-tx")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Bulk create recipes WITHOUT transaction",
            description = "Creates recipes one by one without global transaction. Partial success is possible.")
    public List<RecipeDto> createBulkWithoutTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Two recipes: the second contains an intentionally invalid ingredient id.",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Partial save demonstration",
                                    value = TRANSACTIONAL_BULK_EXAMPLE)))
            @RequestBody @NotEmpty List<@Valid RecipeCreateDto> dtos) {
        return recipeService.createBulkWithoutTransaction(dtos);
    }

    @GetMapping("/views/stats")
    @Operation(summary = "Get recipe view counters statistics",
            description = "Compares AtomicLong (safe), synchronized (safe) and raw int (unsafe) counters to show lost updates caused by race conditions during load testing.")
    public CounterStatsDto getViewCounterStats() {
        return recipeViewCounterService.getStats();
    }

    @PostMapping("/views/reset")
    @Operation(summary = "Reset recipe view counters",
            description = "Resets safe and unsafe view counters to zero before running a load test in JMeter.")
    public ResponseEntity<Void> resetViewCounters() {
        recipeViewCounterService.reset();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/demo/race-condition")
    @Operation(summary = "Demonstrate Race Condition using 50+ threads",
            description = "Executes 50 concurrent threads hammering an unsafe int vs AtomicLong counter to visibly prove race condition and lost updates.")
    public RaceConditionDemoResultDto demonstrateRaceCondition(
            @Parameter(description = "Number of concurrent threads (default 50)", example = "50")
            @RequestParam(defaultValue = "50") int threadCount,
            @Parameter(description = "Number of increments per thread (default 100)", example = "100")
            @RequestParam(defaultValue = "100") int incrementsPerThread) {
        return recipeViewCounterService.demonstrateRaceCondition(threadCount, incrementsPerThread);
    }

    @PostMapping("/{id}/nutrition-report")
    @Operation(summary = "Start asynchronous nutrition calculation via Open Food Facts API",
            description = "Launches a background worker (@Async / CompletableFuture) that fetches nutrition data from Open Food Facts API. Immediately returns 202 Accepted with a task ID; the task remains IN_PROGRESS for at least ten seconds so clients can poll it.")
    public ResponseEntity<AsyncTaskResponseDto> startNutritionReport(
            @Parameter(description = "Recipe ID", example = "1")
            @PathVariable Long id) {
        UUID taskId = nutritionReportService.startNutritionReport(id);
        AsyncTaskResponseDto task = nutritionReportService.getTaskStatus(taskId);
        return ResponseEntity.accepted().body(task);
    }

    @GetMapping("/nutrition-report/{taskId}")
    @Operation(summary = "Check status of asynchronous nutrition report",
            description = "Polls the status of the background task (IN_PROGRESS -> COMPLETED/FAILED) and returns the computed nutritional report.")
    public AsyncTaskResponseDto getNutritionReportStatus(
            @Parameter(description = "Task ID returned by start endpoint", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID taskId) {
        return nutritionReportService.pollTaskStatus(taskId);
    }
}
