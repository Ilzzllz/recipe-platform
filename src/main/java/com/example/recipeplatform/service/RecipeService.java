package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.dto.RecipeRequest;
import com.example.recipeplatform.dto.StepRequest;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.mapper.RecipeMapper;
import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.model.CookingStep;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.User;
import com.example.recipeplatform.repository.CategoryRepository;
import com.example.recipeplatform.repository.IngredientRepository;
import com.example.recipeplatform.repository.RecipeRepository;
import com.example.recipeplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

@Service
public class RecipeService {

    private static final String RECIPE_WITH_ID_PREFIX = "Recipe with id ";
    private static final String USER_WITH_ID_PREFIX = "User with id ";
    private static final String CATEGORY_WITH_ID_PREFIX = "Category with id ";
    private static final String NOT_FOUND_SUFFIX = " was not found";

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeMapper recipeMapper;

    public RecipeService(RecipeRepository recipeRepository,
                         UserRepository userRepository,
                         CategoryRepository categoryRepository,
                         IngredientRepository ingredientRepository,
                         RecipeMapper recipeMapper) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeMapper = recipeMapper;
    }

    @Transactional(readOnly = true)
    public List<RecipeDto> findAll() {
        return recipeMapper.toDtoList(recipeRepository.findAllWithDetails());
    }

    @Transactional(readOnly = true)
    public RecipeDto getById(Long id) {
        return recipeMapper.toDto(findDetailedRecipe(id));
    }

    @Transactional(readOnly = true)
    public List<RecipeDto> searchByTitle(String title) {
        return recipeMapper.toDtoList(recipeRepository.searchWithDetails(title));
    }

    @Transactional
    public RecipeDto create(RecipeRequest request) {
        Recipe recipe = new Recipe();
        applyRequest(recipe, request);
        return recipeMapper.toDto(recipeRepository.save(recipe));
    }

    @Transactional
    public RecipeDto update(Long id, RecipeRequest request) {
        Recipe recipe = findDetailedRecipe(id);
        applyRequest(recipe, request);
        return recipeMapper.toDto(recipeRepository.save(recipe));
    }

    @Transactional
    public void delete(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(RECIPE_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
        recipeRepository.delete(recipe);
    }

    private Recipe findDetailedRecipe(Long id) {
        return recipeRepository.findDetailedById(id)
                .orElseThrow(() -> new NotFoundException(RECIPE_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
    }

    private void applyRequest(Recipe recipe, RecipeRequest request) {
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new NotFoundException(USER_WITH_ID_PREFIX + request.getAuthorId() + NOT_FOUND_SUFFIX));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException(CATEGORY_WITH_ID_PREFIX + request.getCategoryId() + NOT_FOUND_SUFFIX));
        List<Ingredient> ingredients = ingredientRepository.findAllById(request.getIngredientIds());

        if (ingredients.size() != request.getIngredientIds().size()) {
            throw new NotFoundException("One or more ingredients were not found");
        }

        recipe.setTitle(request.getTitle());
        recipe.setDescription(request.getDescription());
        recipe.setAuthor(author);
        recipe.setCategory(category);
        recipe.replaceIngredients(new LinkedHashSet<>(ingredients));
        recipe.replaceSteps(mapSteps(request.getSteps()));
    }

    private List<CookingStep> mapSteps(List<StepRequest> stepRequests) {
        return stepRequests.stream()
                .map(this::toEntity)
                .toList();
    }

    private CookingStep toEntity(StepRequest request) {
        CookingStep step = new CookingStep();
        step.setStepOrder(request.getStepOrder());
        step.setDescription(request.getDescription());
        return step;
    }
}

