package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.mapper.RecipeMapper;
import com.example.recipeplatform.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository repository;
    private final RecipeMapper mapper;

    public RecipeDto getRecipeById(Long id) {
        return mapper.toDto(repository.findById(id));
    }

    public String searchInfo(String title) {
        return "Вы ищете рецепт: " + title;
    }
}
