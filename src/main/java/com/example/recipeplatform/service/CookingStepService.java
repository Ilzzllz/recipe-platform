package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.CookingStepDto;
import com.example.recipeplatform.dto.CookingStepRequest;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.mapper.RecipeMapper;
import com.example.recipeplatform.model.CookingStep;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.repository.CookingStepRepository;
import com.example.recipeplatform.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CookingStepService {

    private static final String RECIPE_WITH_ID_PREFIX = "Recipe with id ";
    private static final String COOKING_STEP_WITH_ID_PREFIX = "Cooking step with id ";
    private static final String NOT_FOUND_SUFFIX = " was not found";

    private final CookingStepRepository cookingStepRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;

    public CookingStepService(CookingStepRepository cookingStepRepository,
                              RecipeRepository recipeRepository,
                              RecipeMapper recipeMapper) {
        this.cookingStepRepository = cookingStepRepository;
        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
    }

    @Transactional(readOnly = true)
    public List<CookingStepDto> findAll() {
        return cookingStepRepository.findAll().stream()
                .map(recipeMapper::toStepDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CookingStepDto findById(Long id) {
        return recipeMapper.toStepDto(findStep(id));
    }

    @Transactional
    public CookingStepDto create(CookingStepRequest request) {
        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new NotFoundException(RECIPE_WITH_ID_PREFIX + request.getRecipeId() + NOT_FOUND_SUFFIX));
        CookingStep step = new CookingStep();
        step.setRecipe(recipe);
        step.setStepOrder(request.getStepOrder());
        step.setDescription(request.getDescription());
        return recipeMapper.toStepDto(cookingStepRepository.save(step));
    }

    @Transactional
    public CookingStepDto update(Long id, CookingStepRequest request) {
        CookingStep step = findStep(id);
        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new NotFoundException(RECIPE_WITH_ID_PREFIX + request.getRecipeId() + NOT_FOUND_SUFFIX));
        step.setRecipe(recipe);
        step.setStepOrder(request.getStepOrder());
        step.setDescription(request.getDescription());
        return recipeMapper.toStepDto(cookingStepRepository.save(step));
    }

    @Transactional
    public void delete(Long id) {
        cookingStepRepository.delete(findStep(id));
    }

    private CookingStep findStep(Long id) {
        return cookingStepRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(COOKING_STEP_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
    }
}
