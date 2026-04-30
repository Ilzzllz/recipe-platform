package com.example.recipeplatform.controller;

import com.example.recipeplatform.dto.CategoryCreateDto;
import com.example.recipeplatform.dto.CategoryDto;
import com.example.recipeplatform.exception.ApiError;
import com.example.recipeplatform.service.CategoryService;
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
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "CRUD operations for categories")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Validation error or duplicate category name",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Category was not found",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "Database constraint conflict",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
})
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Get all categories")
    public List<CategoryDto> getAll() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by id")
    public CategoryDto getById(@Parameter(description = "Existing category id", example = "1")
                               @PathVariable Long id) {
        return categoryService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new category")
    public CategoryDto create(@Valid @RequestBody CategoryCreateDto dto) {
        return categoryService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category by id", description = "PUT performs a full replacement of editable category fields.")
    public CategoryDto update(@Parameter(description = "Existing category id", example = "1")
                              @PathVariable Long id,
                              @Valid @RequestBody CategoryCreateDto dto) {
        return categoryService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete category by id")
    @ApiResponse(responseCode = "204", description = "Category deleted successfully")
    public void delete(@Parameter(description = "Existing category id", example = "1")
                       @PathVariable Long id) {
        categoryService.delete(id);
    }
}
