package com.example.recipeplatform.controller;

import com.example.recipeplatform.dto.NPlusOneDemoResponse;
import com.example.recipeplatform.dto.RecipeCreateDto;
import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Recipes", description = "CRUD operations for recipes")
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
    public RecipeDto getById(@PathVariable Long id) {
        return recipeService.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search recipes by title")
    public List<RecipeDto> getByTitle(@RequestParam String title) {
        return recipeService.searchByTitle(title);
    }

    @GetMapping("/n-plus-one/problem")
    @Operation(summary = "Demonstrate the N+1 problem")
    public NPlusOneDemoResponse showNPlusOneProblem() {
        return recipeService.demonstrateNPlusOneProblem();
    }

    @GetMapping("/n-plus-one/solution")
    @Operation(summary = "Show the fetch join solution for N+1")
    public NPlusOneDemoResponse showNPlusOneSolution() {
        return recipeService.demonstrateNPlusOneSolution();
    }

    @PostMapping("/transactions/without-transactional")
    @Operation(summary = "Demonstrate partial save without @Transactional")
    public void demonstratePartialSave() {
        recipeService.demonstratePartialSaveWithoutTransactional();
    }

    @PostMapping("/transactions/with-transactional")
    @Operation(summary = "Demonstrate rollback with @Transactional")
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
    @Operation(summary = "Update an existing recipe")
    public RecipeDto update(@PathVariable Long id, @Valid @RequestBody RecipeCreateDto request) {
        return recipeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete recipe by id")
    public void delete(@PathVariable Long id) {
        recipeService.delete(id);
    }
}
