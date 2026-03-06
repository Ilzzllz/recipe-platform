package com.example.recipeplatform.controller;

import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService service;

    // Пункт 3.1: @PathVariable
    // Ссылка: http://localhost:8080/api/recipes/1
    @GetMapping("/{id}")
    public RecipeDto getById(@PathVariable Long id) {
        return service.getRecipeById(id);
    }

    // Пункт 3.2: @RequestParam
    // Ссылка: http://localhost:8080/api/recipes/search?title=Борщ
    @GetMapping("/search")
    public String search(@RequestParam String title) {
        return service.searchInfo(title);
    }
}
