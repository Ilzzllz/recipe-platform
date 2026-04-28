package com.example.recipeplatform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

public class RecipeRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Long authorId;

    @NotNull
    private Long categoryId;

    @NotEmpty
    private Set<Long> ingredientIds;

    @Valid
    @NotEmpty
    private List<StepRequest> steps;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Set<Long> getIngredientIds() {
        return ingredientIds;
    }

    public void setIngredientIds(Set<Long> ingredientIds) {
        this.ingredientIds = ingredientIds;
    }

    public List<StepRequest> getSteps() {
        return steps;
    }

    public void setSteps(List<StepRequest> steps) {
        this.steps = steps;
    }
}
