package com.example.recipeplatform.controller;

import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    @GetMapping("/{id}")
    public RecipeDto getById(@PathVariable Long id) {
        return recipeService.getById(id);
    }

    @GetMapping("/search")
    public List<RecipeDto> getByTitle(@RequestParam String title) {

        return recipeService.searchByTitle(title);
    }
}
