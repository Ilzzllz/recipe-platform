package com.example.recipeplatform.controller;

import com.example.recipeplatform.dto.NPlusOneDemoResponse;
import com.example.recipeplatform.dto.RecipeCreateDto;
import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.dto.TransactionDemoResponse;
import com.example.recipeplatform.exception.ApiError;
import com.example.recipeplatform.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    @Operation(summary = "Get all recipes")
    public List<RecipeDto> getAll() {
        return recipeService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recipe by id")
    public RecipeDto getById(@Parameter(description = "Existing recipe id", example = "12")
                             @PathVariable Long id) {
        return recipeService.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search recipes by title")
    public List<RecipeDto> getByTitle(@Parameter(description = "Part of the recipe title", example = "soup")
                                      @RequestParam String title) {
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

    @PostMapping("/transactions/without-transactional")
    @Operation(summary = "Demonstrate partial save without @Transactional",
            description = "This endpoint intentionally triggers a failure after saving related data. Because there is no transaction, part of the data remains in the database.")
    @ApiResponse(responseCode = "200", description = "Partial save demo completed",
            content = @Content(schema = @Schema(implementation = TransactionDemoResponse.class)))
    public TransactionDemoResponse demonstratePartialSave() {
        return recipeService.demonstratePartialSaveWithoutTransactional();
    }

    @PostMapping("/transactions/with-transactional")
    @Operation(summary = "Demonstrate rollback with @Transactional",
            description = "This endpoint intentionally throws an error. Because the method is transactional, all inserted data is rolled back.")
    @ApiResponse(responseCode = "500", description = "Intentional rollback demo error",
            content = @Content(schema = @Schema(implementation = TransactionDemoResponse.class)))
    public void demonstrateRollback() {
        recipeService.demonstrateRollbackWithTransactional();
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
    @Operation(summary = "Filter recipes by author username (JPQL + pagination)",
            description = "Uses JPQL query with JOIN FETCH. Pagination and in-memory cache are applied.")
    public Page<RecipeDto> filterByAuthorJPQL(
            @Parameter(description = "Author username (case-insensitive)", example = "anna")
            @RequestParam String authorUsername,
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return recipeService.findByAuthorJPQL(authorUsername, pageable);
    }

    @GetMapping("/filter/native")
    @Operation(summary = "Filter recipes by author username (native SQL + pagination)",
            description = "Uses native SQL query with LIMIT/OFFSET. Pagination and in-memory cache are applied.")
    public Page<RecipeDto> filterByAuthorNative(
            @Parameter(description = "Author username (case-insensitive)", example = "anna")
            @RequestParam String authorUsername,
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return recipeService.findByAuthorNative(authorUsername, pageable);
    }
}