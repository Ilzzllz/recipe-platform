package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.CookingStepDto;
import com.example.recipeplatform.dto.CookingStepCreateDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.mapper.CookingStepMapper;
import com.example.recipeplatform.model.CookingStep;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.repository.CookingStepRepository;
import com.example.recipeplatform.repository.RecipeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CookingStepService {

    private static final String RECIPE_WITH_ID_PREFIX = "Recipe with id ";
    private static final String COOKING_STEP_WITH_ID_PREFIX = "Cooking step with id ";
    private static final String NOT_FOUND_SUFFIX = " was not found";
    private static final Sort SORT_BY_ID = Sort.by(Sort.Direction.ASC, "id");

    private final CookingStepRepository cookingStepRepository;
    private final RecipeRepository recipeRepository;
    private final CookingStepMapper cookingStepMapper;

    public CookingStepService(CookingStepRepository cookingStepRepository,
                              RecipeRepository recipeRepository,
                              CookingStepMapper cookingStepMapper) {
        this.cookingStepRepository = cookingStepRepository;
        this.recipeRepository = recipeRepository;
        this.cookingStepMapper = cookingStepMapper;
    }

    @Transactional(readOnly = true)
    public List<CookingStepDto> findAll() {
        return cookingStepMapper.toDtoList(cookingStepRepository.findAll(SORT_BY_ID));
    }

    @Transactional(readOnly = true)
    public CookingStepDto findById(Long id) {
        return cookingStepMapper.toDto(findStep(id));
    }

    @Transactional
    public CookingStepDto create(CookingStepCreateDto request) {
        Recipe recipe = findRecipe(resolveRecipeId(request));
        CookingStep step = cookingStepMapper.toEntity(request);
        step.setRecipe(recipe);
        return cookingStepMapper.toDto(cookingStepRepository.save(step));
    }

    @Transactional
    public CookingStepDto update(Long id, CookingStepCreateDto request) {
        CookingStep step = findStep(id);
        Recipe recipe = findRecipe(resolveRecipeId(request));
        cookingStepMapper.updateEntity(step, request);
        step.setRecipe(recipe);
        return cookingStepMapper.toDto(cookingStepRepository.save(step));
    }

    @Transactional
    public void delete(Long id) {
        cookingStepRepository.delete(findStep(id));
    }

    private CookingStep findStep(Long id) {
        return cookingStepRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(COOKING_STEP_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
    }

    private Recipe findRecipe(Long recipeId) {
        return recipeRepository.findById(recipeId)
                .orElseThrow(() -> new NotFoundException(RECIPE_WITH_ID_PREFIX + recipeId + NOT_FOUND_SUFFIX));
    }

    private Long resolveRecipeId(CookingStepCreateDto request) {
        if (request.getRecipeId() == null) {
            throw new IllegalArgumentException("recipeId is required for cooking step requests");
        }
        return request.getRecipeId();
    }
}
