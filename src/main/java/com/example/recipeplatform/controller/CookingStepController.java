package com.example.recipeplatform.controller;

import com.example.recipeplatform.dto.CookingStepDto;
import com.example.recipeplatform.dto.CookingStepRequest;
import com.example.recipeplatform.service.CookingStepService;
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
@RequestMapping("/api/steps")
@Tag(name = "Cooking Steps", description = "CRUD operations for cooking steps")
public class CookingStepController {

    private final CookingStepService cookingStepService;

    public CookingStepController(CookingStepService cookingStepService) {
        this.cookingStepService = cookingStepService;
    }

    @GetMapping
    @Operation(summary = "Get all cooking steps")
    public List<CookingStepDto> getAll() {
        return cookingStepService.findAll();
    }

    @GetMapping("/{id}")
    public CookingStepDto getById(@PathVariable Long id) {
        return cookingStepService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new cooking step")
    public CookingStepDto create(@Valid @RequestBody CookingStepRequest request) {
        return cookingStepService.create(request);
    }

    @PutMapping("/{id}")
    public CookingStepDto update(@PathVariable Long id, @Valid @RequestBody CookingStepRequest request) {
        return cookingStepService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        cookingStepService.delete(id);
    }
}
