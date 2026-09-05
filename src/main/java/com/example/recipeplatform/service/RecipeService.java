package com.example.recipeplatform.service;

import com.example.recipeplatform.cache.CacheKey;
import com.example.recipeplatform.cache.RecipeQueryCacheService;
import com.example.recipeplatform.dto.*;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.mapper.CookingStepMapper;
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
import com.example.recipeplatform.repository.projection.RecipeFilterProjection;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final CookingStepMapper cookingStepMapper;
    private final EntityManagerFactory entityManagerFactory;
    private final RecipeQueryCacheService recipeQueryCacheService;

    public RecipeService(RecipeRepository recipeRepository,
                         UserRepository userRepository,
                         CategoryRepository categoryRepository,
                         IngredientRepository ingredientRepository,
                         RecipeMapper recipeMapper,
                         CookingStepMapper cookingStepMapper,
                         EntityManagerFactory entityManagerFactory,
                         RecipeQueryCacheService recipeQueryCacheService) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeMapper = recipeMapper;
        this.cookingStepMapper = cookingStepMapper;
        this.entityManagerFactory = entityManagerFactory;
        this.recipeQueryCacheService = recipeQueryCacheService;
    }

    @Transactional(readOnly = true)
    public List<RecipeDto> findAll() {
        return recipeMapper.toDtoList(recipeRepository.findAllWithFetchJoin());
    }

    @Transactional(readOnly = true)
    public RecipeDto getById(Long id) {
        return recipeMapper.toDto(findDetailedRecipe(id));
    }

    @Transactional(readOnly = true)
    public List<RecipeDto> searchByTitle(String title) {
        return recipeMapper.toDtoList(recipeRepository.searchWithFetchJoin(title));
    }

    @Transactional
    public RecipeDto create(RecipeCreateDto request) {
        Recipe recipe = recipeMapper.toEntity(request);
        applyRequest(recipe, request);
        RecipeDto result = recipeMapper.toDto(recipeRepository.save(recipe));
        recipeQueryCacheService.invalidateAll();
        return result;
    }

    @Transactional
    public RecipeDto update(Long id, RecipeCreateDto request) {
        Recipe recipe = findDetailedRecipe(id);
        applyRequest(recipe, request);
        RecipeDto result = recipeMapper.toDto(recipeRepository.save(recipe));
        recipeQueryCacheService.invalidateAll();
        return result;
    }

    @Transactional
    public void delete(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(RECIPE_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
        recipeRepository.delete(recipe);
        recipeQueryCacheService.invalidateAll();
    }

    @Transactional(readOnly = true)
    public NPlusOneDemoResponse demonstrateNPlusOneProblem() {
        Statistics statistics = statistics();
        statistics.clear();
        List<RecipeDto> recipes = recipeMapper.toDtoList(recipeRepository.findAll());
        return buildNPlusOneResponse("N+1 problem", recipes, statistics.getPrepareStatementCount());
    }

    @Transactional(readOnly = true)
    public NPlusOneDemoResponse demonstrateNPlusOneSolution() {
        Statistics statistics = statistics();
        statistics.clear();
        List<RecipeDto> recipes = recipeMapper.toDtoList(recipeRepository.findAllWithFetchJoin());
        return buildNPlusOneResponse("Fetch join solution", recipes, statistics.getPrepareStatementCount());
    }

    @Transactional(readOnly = true)
    public Page<RecipeFilterDto> findByAuthorAndCategoryJPQL(String authorUsername,
                                                             String categoryName,
                                                             Pageable pageable) {
        CacheKey key = CacheKey.from("jpql", authorUsername, categoryName, pageable);
        Page<RecipeFilterDto> cached = recipeQueryCacheService.get(key);
        if (cached != null) {
            return cached;
        }
        Page<RecipeFilterDto> result = recipeRepository.findByAuthorUsernameJPQL(authorUsername, categoryName, pageable)
                .map(this::mapFilterProjection);
        recipeQueryCacheService.put(key, result);
        return result;
    }

    @Transactional(readOnly = true)
    public Page<RecipeFilterDto> findByAuthorAndCategoryNative(String authorUsername,
                                                               String categoryName,
                                                               Pageable pageable) {
        CacheKey key = CacheKey.from("native", authorUsername, categoryName, pageable);
        Page<RecipeFilterDto> cached = recipeQueryCacheService.get(key);
        if (cached != null) {
            return cached;
        }
        Page<RecipeFilterDto> result = recipeRepository.findByAuthorUsernameNative(authorUsername, categoryName, pageable)
                .map(this::mapFilterProjection);
        recipeQueryCacheService.put(key, result);
        return result;
    }

    private Recipe findDetailedRecipe(Long id) {
        return recipeRepository.findByIdWithFetchJoin(id)
                .orElseThrow(() -> new NotFoundException(RECIPE_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
    }

    private void applyRequest(Recipe recipe, RecipeCreateDto request) {
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new NotFoundException(USER_WITH_ID_PREFIX + request.getAuthorId() + NOT_FOUND_SUFFIX));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException(CATEGORY_WITH_ID_PREFIX + request.getCategoryId() + NOT_FOUND_SUFFIX));
        List<Ingredient> ingredients = ingredientRepository.findAllById(request.getIngredientIds());
        if (ingredients.size() != request.getIngredientIds().size()) {
            throw new NotFoundException("One or more ingredients were not found");
        }
        recipeMapper.updateEntity(recipe, request);
        recipe.setAuthor(author);
        recipe.setCategory(category);
        recipe.replaceIngredients(new LinkedHashSet<>(ingredients));
        recipe.replaceSteps(mapSteps(request.getSteps()));
    }

    private List<CookingStep> mapSteps(List<RecipeStepCreateDto> stepRequests) {
        return stepRequests.stream()
                .map(cookingStepMapper::toEntity)
                .toList();
    }

    private RecipeFilterDto mapFilterProjection(RecipeFilterProjection projection) {
        RecipeFilterDto dto = new RecipeFilterDto();
        dto.setId(projection.getRecipeId());
        dto.setTitle(projection.getRecipeTitle());
        dto.setDescription(projection.getRecipeDescription());

        AuthorReferenceDto authorReferenceDto = new AuthorReferenceDto();
        authorReferenceDto.setId(projection.getAuthorId());
        authorReferenceDto.setUsername(projection.getAuthorUsername());
        dto.setAuthor(authorReferenceDto);

        CategoryReferenceDto categoryReferenceDto = new CategoryReferenceDto();
        categoryReferenceDto.setId(projection.getCategoryId());
        categoryReferenceDto.setName(projection.getCategoryName());
        dto.setCategory(categoryReferenceDto);
        return dto;
    }

    private NPlusOneDemoResponse buildNPlusOneResponse(String scenario,
                                                       List<RecipeDto> recipes,
                                                       long statementCount) {
        NPlusOneDemoResponse response = new NPlusOneDemoResponse();
        response.setScenario(scenario);
        response.setSqlStatements(statementCount);
        response.setRecipesLoaded(recipes.size());
        response.setRecipes(recipes);
        return response;
    }

    private Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }
}
