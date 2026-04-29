package com.example.recipeplatform.controller;

import com.example.recipeplatform.dto.IngredientCreateDto;
import com.example.recipeplatform.dto.IngredientDto;
import com.example.recipeplatform.service.IngredientService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@Tag(name = "Ingredients", description = "CRUD operations for ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    @Operation(summary = "Get all ingredients")
    public List<IngredientDto> getAll() {
        return ingredientService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ingredient by id")
    public IngredientDto getById(@PathVariable Long id) {
        return ingredientService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new ingredient")
    public IngredientDto create(@Valid @RequestBody IngredientCreateDto dto) {
        return ingredientService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ingredient by id")
    public IngredientDto update(@PathVariable Long id, @Valid @RequestBody IngredientCreateDto dto) {
        return ingredientService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete ingredient by id")
    public void delete(@PathVariable Long id) {
        ingredientService.delete(id);
    }
}
