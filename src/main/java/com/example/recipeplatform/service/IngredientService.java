package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.IngredientCreateDto;
import com.example.recipeplatform.dto.IngredientDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.mapper.IngredientMapper;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class IngredientService {

    private static final String INGREDIENT_WITH_ID_PREFIX = "Ingredient with id ";
    private static final String NOT_FOUND_SUFFIX = " was not found";

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    public IngredientService(IngredientRepository ingredientRepository, IngredientMapper ingredientMapper) {
        this.ingredientRepository = ingredientRepository;
        this.ingredientMapper = ingredientMapper;
    }

    @Transactional(readOnly = true)
    public List<IngredientDto> findAll() {
        return ingredientRepository.findAll().stream()
                .map(ingredientMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public IngredientDto getById(Long id) {
        return ingredientMapper.toDto(findEntity(id));
    }

    @Transactional
    public IngredientDto create(IngredientCreateDto dto) {
        if (ingredientRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Ingredient already exists");
        }
        return ingredientMapper.toDto(ingredientRepository.save(ingredientMapper.toEntity(dto)));
    }

    @Transactional
    public IngredientDto update(Long id, IngredientCreateDto dto) {
        Ingredient ingredient = findEntity(id);
        ingredientMapper.updateEntity(ingredient, dto);
        return ingredientMapper.toDto(ingredientRepository.save(ingredient));
    }

    @Transactional
    public void delete(Long id) {
        Ingredient ingredient = findEntity(id);
        Set<Recipe> recipes = new LinkedHashSet<>(ingredient.getRecipes());
        for (Recipe recipe : recipes) {
            recipe.getIngredients().remove(ingredient);
        }
        ingredient.getRecipes().clear();
        ingredientRepository.delete(ingredient);
    }

    private Ingredient findEntity(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(INGREDIENT_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
    }
}
