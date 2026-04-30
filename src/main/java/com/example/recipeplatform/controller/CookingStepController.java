package com.example.recipeplatform.controller;

import com.example.recipeplatform.dto.CookingStepCreateDto;
import com.example.recipeplatform.dto.CookingStepDto;
import com.example.recipeplatform.exception.ApiError;
import com.example.recipeplatform.service.CookingStepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/cooking-steps")
@Tag(name = "Cooking Steps", description = "CRUD operations for cooking steps")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Validation error in cooking step data",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Cooking step or recipe was not found",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "Database constraint conflict",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
})
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
    @Operation(summary = "Get cooking step by id")
    public CookingStepDto getById(@Parameter(description = "Existing cooking step id", example = "25")
                                  @PathVariable Long id) {
        return cookingStepService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new cooking step")
    public CookingStepDto create(@Valid @RequestBody CookingStepCreateDto request) {
        return cookingStepService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update cooking step by id", description = "PUT performs a full replacement of editable cooking step fields.")
    public CookingStepDto update(@Parameter(description = "Existing cooking step id", example = "25")
                                 @PathVariable Long id,
                                 @Valid @RequestBody CookingStepCreateDto request) {
        return cookingStepService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete cooking step by id")
    @ApiResponse(responseCode = "204", description = "Cooking step deleted successfully")
    public void delete(@Parameter(description = "Existing cooking step id", example = "25")
                       @PathVariable Long id) {
        cookingStepService.delete(id);
    }
}
