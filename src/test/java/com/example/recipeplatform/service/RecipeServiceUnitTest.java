package com.example.recipeplatform.service;

import com.example.recipeplatform.cache.CacheKey;
import com.example.recipeplatform.cache.RecipeQueryCacheService;
import com.example.recipeplatform.dto.NPlusOneDemoResponse;
import com.example.recipeplatform.dto.RecipeCreateDto;
import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.dto.RecipeFilterDto;
import com.example.recipeplatform.dto.RecipeStepCreateDto;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceUnitTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private RecipeMapper recipeMapper;

    @Mock
    private CookingStepMapper cookingStepMapper;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Statistics statistics;

    @Mock
    private RecipeQueryCacheService recipeQueryCacheService;

    private RecipeService recipeService;

    @BeforeEach
    void setUp() {
        recipeService = new RecipeService(recipeRepository, userRepository, categoryRepository,
                ingredientRepository, recipeMapper, cookingStepMapper, entityManagerFactory,
                recipeQueryCacheService);
    }

    @Test
    @DisplayName("findAll should return all recipes via recipeMapper")
    void findAllShouldReturnMappedList() {
        Recipe recipe = new Recipe();
        RecipeDto dto = new RecipeDto();
        when(recipeRepository.findAllWithFetchJoin()).thenReturn(List.of(recipe));
        when(recipeMapper.toDtoList(List.of(recipe))).thenReturn(List.of(dto));

        List<RecipeDto> result = recipeService.findAll();

        assertThat(result).containsExactly(dto);
        verify(recipeRepository).findAllWithFetchJoin();
    }

    @Test
    @DisplayName("getById should return recipe DTO when found")
    void getByIdShouldReturnRecipeDtoWhenFound() {
        Recipe recipe = new Recipe();
        RecipeDto dto = new RecipeDto();
        when(recipeRepository.findByIdWithFetchJoin(1L)).thenReturn(Optional.of(recipe));
        when(recipeMapper.toDto(recipe)).thenReturn(dto);

        RecipeDto result = recipeService.getById(1L);

        assertThat(result).isSameAs(dto);
    }

    @Test
    @DisplayName("getById should throw NotFoundException when recipe does not exist")
    void getByIdShouldThrowNotFoundException() {
        when(recipeRepository.findByIdWithFetchJoin(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Recipe with id 999 was not found");
    }

    @Test
    @DisplayName("searchByTitle should return matching recipe DTOs")
    void searchByTitleShouldReturnMatchingRecipes() {
        Recipe recipe = new Recipe();
        RecipeDto dto = new RecipeDto();
        when(recipeRepository.searchWithFetchJoin("soup")).thenReturn(List.of(recipe));
        when(recipeMapper.toDtoList(List.of(recipe))).thenReturn(List.of(dto));

        List<RecipeDto> result = recipeService.searchByTitle("soup");

        assertThat(result).containsExactly(dto);
    }

    @Test
    @DisplayName("create should map, apply relations, save recipe and invalidate cache")
    void createShouldSaveRecipeAndInvalidateCache() {
        RecipeCreateDto request = validRecipeRequest("Borscht");
        Recipe recipe = new Recipe();
        Recipe savedRecipe = new Recipe();
        RecipeDto dto = new RecipeDto();

        stubAuthorAndCategory();
        Ingredient ingredient = new Ingredient();
        ingredient.setId(3L);
        when(ingredientRepository.findAllById(Set.of(3L))).thenReturn(List.of(ingredient));

        when(recipeMapper.toEntity(request)).thenReturn(recipe);
        when(cookingStepMapper.toEntity(any(RecipeStepCreateDto.class))).thenReturn(new CookingStep());
        when(recipeRepository.save(recipe)).thenReturn(savedRecipe);
        when(recipeMapper.toDto(savedRecipe)).thenReturn(dto);

        RecipeDto result = recipeService.create(request);

        assertThat(result).isSameAs(dto);
        verify(recipeRepository).save(recipe);
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("create should throw NotFoundException when author does not exist")
    void createShouldThrowWhenAuthorNotFound() {
        RecipeCreateDto request = validRecipeRequest("Borscht");
        Recipe recipe = new Recipe();
        when(recipeMapper.toEntity(request)).thenReturn(recipe);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with id 1 was not found");
    }

    @Test
    @DisplayName("create should throw NotFoundException when category does not exist")
    void createShouldThrowWhenCategoryNotFound() {
        RecipeCreateDto request = validRecipeRequest("Borscht");
        Recipe recipe = new Recipe();
        User user = new User();
        user.setId(1L);

        when(recipeMapper.toEntity(request)).thenReturn(recipe);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category with id 2 was not found");
    }

    @Test
    @DisplayName("create should throw NotFoundException when ingredient count does not match")
    void createShouldThrowWhenIngredientNotFound() {
        RecipeCreateDto request = validRecipeRequest("Borscht");
        Recipe recipe = new Recipe();
        User user = new User();
        user.setId(1L);
        Category category = new Category();
        category.setId(2L);

        when(recipeMapper.toEntity(request)).thenReturn(recipe);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(ingredientRepository.findAllById(Set.of(3L))).thenReturn(List.of()); // 0 ingredients found vs 1 requested

        assertThatThrownBy(() -> recipeService.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("One or more ingredients were not found");
    }

    @Test
    @DisplayName("update should update existing recipe and invalidate cache")
    void updateShouldModifyRecipeAndInvalidateCache() {
        RecipeCreateDto request = validRecipeRequest("Updated Soup");
        Recipe existing = new Recipe();
        Recipe saved = new Recipe();
        RecipeDto dto = new RecipeDto();

        when(recipeRepository.findByIdWithFetchJoin(10L)).thenReturn(Optional.of(existing));
        stubAuthorAndCategory();
        Ingredient ingredient = new Ingredient();
        ingredient.setId(3L);
        when(ingredientRepository.findAllById(Set.of(3L))).thenReturn(List.of(ingredient));
        when(cookingStepMapper.toEntity(any(RecipeStepCreateDto.class))).thenReturn(new CookingStep());
        when(recipeRepository.save(existing)).thenReturn(saved);
        when(recipeMapper.toDto(saved)).thenReturn(dto);

        RecipeDto result = recipeService.update(10L, request);

        assertThat(result).isSameAs(dto);
        verify(recipeMapper).updateEntity(existing, request);
        verify(recipeRepository).save(existing);
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("delete should remove existing recipe and invalidate cache")
    void deleteShouldRemoveRecipeAndInvalidateCache() {
        Recipe recipe = new Recipe();
        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipe));

        recipeService.delete(10L);

        verify(recipeRepository).delete(recipe);
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("delete should throw NotFoundException when recipe does not exist")
    void deleteShouldThrowNotFoundException() {
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.delete(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Recipe with id 999 was not found");
    }

    @Test
    @DisplayName("demonstrateNPlusOneProblem should return stats and DTOs")
    void demonstrateNPlusOneProblemShouldReturnResponse() {
        stubHibernateStatistics();
        when(recipeRepository.findAll()).thenReturn(List.of());
        when(recipeMapper.toDtoList(List.of())).thenReturn(List.of());
        when(statistics.getPrepareStatementCount()).thenReturn(5L);

        NPlusOneDemoResponse response = recipeService.demonstrateNPlusOneProblem();

        assertThat(response.getScenario()).isEqualTo("N+1 problem");
        assertThat(response.getSqlStatements()).isEqualTo(5L);
    }

    @Test
    @DisplayName("demonstrateNPlusOneSolution should return stats and DTOs with fetch join")
    void demonstrateNPlusOneSolutionShouldReturnResponse() {
        stubHibernateStatistics();
        when(recipeRepository.findAllWithFetchJoin()).thenReturn(List.of());
        when(recipeMapper.toDtoList(List.of())).thenReturn(List.of());
        when(statistics.getPrepareStatementCount()).thenReturn(1L);

        NPlusOneDemoResponse response = recipeService.demonstrateNPlusOneSolution();

        assertThat(response.getScenario()).isEqualTo("Fetch join solution");
        assertThat(response.getSqlStatements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByAuthorAndCategoryJPQL should return cached page if available")
    void findByAuthorAndCategoryJPQLShouldReturnFromCache() {
        Pageable pageable = PageRequest.of(0, 10);
        CacheKey key = CacheKey.from("jpql", "anna", "soups", pageable);
        Page<RecipeFilterDto> cachedPage = new PageImpl<>(List.of(new RecipeFilterDto()));

        when(recipeQueryCacheService.get(key)).thenReturn(cachedPage);

        Page<RecipeFilterDto> result = recipeService.findByAuthorAndCategoryJPQL("anna", "soups", pageable);

        assertThat(result).isSameAs(cachedPage);
        verify(recipeRepository, never()).findByAuthorUsernameJPQL(any(), any(), any());
    }

    @Test
    @DisplayName("findByAuthorAndCategoryJPQL should query repository and cache result when not in cache")
    void findByAuthorAndCategoryJPQLShouldQueryRepositoryAndCache() {
        Pageable pageable = PageRequest.of(0, 10);
        CacheKey key = CacheKey.from("jpql", "anna", "soups", pageable);
        when(recipeQueryCacheService.get(key)).thenReturn(null);

        RecipeFilterProjection projection = mock(RecipeFilterProjection.class);
        when(projection.getRecipeId()).thenReturn(1L);
        when(projection.getRecipeTitle()).thenReturn("Soup");
        when(projection.getRecipeDescription()).thenReturn("Good soup");
        when(projection.getAuthorId()).thenReturn(10L);
        when(projection.getAuthorUsername()).thenReturn("anna");
        when(projection.getCategoryId()).thenReturn(20L);
        when(projection.getCategoryName()).thenReturn("soups");

        Page<RecipeFilterProjection> projectionPage = new PageImpl<>(List.of(projection));
        when(recipeRepository.findByAuthorUsernameJPQL("anna", "soups", pageable)).thenReturn(projectionPage);

        Page<RecipeFilterDto> result = recipeService.findByAuthorAndCategoryJPQL("anna", "soups", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Soup");
        verify(recipeQueryCacheService).put(eq(key), any());
    }

    @Test
    @DisplayName("findByAuthorAndCategoryNative should return cached page if available")
    void findByAuthorAndCategoryNativeShouldReturnFromCache() {
        Pageable pageable = PageRequest.of(0, 10);
        CacheKey key = CacheKey.from("native", "anna", "soups", pageable);
        Page<RecipeFilterDto> cachedPage = new PageImpl<>(List.of(new RecipeFilterDto()));

        when(recipeQueryCacheService.get(key)).thenReturn(cachedPage);

        Page<RecipeFilterDto> result = recipeService.findByAuthorAndCategoryNative("anna", "soups", pageable);

        assertThat(result).isSameAs(cachedPage);
        verify(recipeRepository, never()).findByAuthorUsernameNative(any(), any(), any());
    }

    @Test
    @DisplayName("findByAuthorAndCategoryNative should query repository and cache result when not in cache")
    void findByAuthorAndCategoryNativeShouldQueryRepositoryAndCache() {
        Pageable pageable = PageRequest.of(0, 10);
        CacheKey key = CacheKey.from("native", "anna", "soups", pageable);
        when(recipeQueryCacheService.get(key)).thenReturn(null);

        RecipeFilterProjection projection = mock(RecipeFilterProjection.class);
        when(projection.getRecipeId()).thenReturn(2L);
        when(projection.getRecipeTitle()).thenReturn("Native Soup");
        when(projection.getRecipeDescription()).thenReturn("Delicious");
        when(projection.getAuthorId()).thenReturn(10L);
        when(projection.getAuthorUsername()).thenReturn("anna");
        when(projection.getCategoryId()).thenReturn(20L);
        when(projection.getCategoryName()).thenReturn("soups");

        Page<RecipeFilterProjection> projectionPage = new PageImpl<>(List.of(projection));
        when(recipeRepository.findByAuthorUsernameNative("anna", "soups", pageable)).thenReturn(projectionPage);

        Page<RecipeFilterDto> result = recipeService.findByAuthorAndCategoryNative("anna", "soups", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Native Soup");
        verify(recipeQueryCacheService).put(eq(key), any());
    }

    @Test
    @DisplayName("createBulk should save all recipes and invalidate query cache")
    void createBulkShouldSaveEveryRecipeAndInvalidateQueryCache() {
        stubReferences();
        when(recipeRepository.saveAndFlush(any(Recipe.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(recipeMapper.toDto(any(Recipe.class))).thenReturn(new RecipeDto());

        List<RecipeDto> result = recipeService.createBulk(List.of(validRecipeRequest("First soup"), validRecipeRequest("Second soup")));

        ArgumentCaptor<Recipe> recipes = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository, times(2)).saveAndFlush(recipes.capture());
        verify(recipeQueryCacheService).invalidateAll();
        assertThat(result).hasSize(2);
        assertThat(recipes.getAllValues()).extracting(Recipe::getTitle)
                .containsExactly("First soup", "Second soup");
    }

    @Test
    @DisplayName("createBulkWithoutTransaction should keep first save attempt when second recipe is invalid")
    void createBulkWithoutTransactionShouldKeepFirstSaveAttemptWhenSecondRecipeIsInvalid() {
        stubReferences();
        when(recipeRepository.saveAndFlush(any(Recipe.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RecipeCreateDto invalid = validRecipeRequest("Broken soup");
        invalid.setIngredientIds(Set.of(999L));
        when(ingredientRepository.findById(999L)).thenReturn(Optional.empty());
        List<RecipeCreateDto> recipes = List.of(validRecipeRequest("Saved soup"), invalid);

        assertThatThrownBy(() -> recipeService.createBulkWithoutTransaction(recipes))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ingredient with id 999");

        verify(recipeRepository).saveAndFlush(any(Recipe.class));
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("createBulk should stop before saving when author is not found")
    void createBulkShouldStopBeforeSavingWhenOptionalAuthorIsEmpty() {
        RecipeCreateDto request = validRecipeRequest("No author soup");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        List<RecipeCreateDto> recipes = List.of(request);

        assertThatThrownBy(() -> recipeService.createBulk(recipes))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with id 1");

        verify(recipeRepository, never()).saveAndFlush(any(Recipe.class));
        verify(recipeQueryCacheService).invalidateAll();
    }

    private void stubHibernateStatistics() {
        when(entityManagerFactory.unwrap(SessionFactory.class)).thenReturn(sessionFactory);
        when(sessionFactory.getStatistics()).thenReturn(statistics);
    }

    private void stubAuthorAndCategory() {
        User user = new User();
        user.setId(1L);
        Category category = new Category();
        category.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
    }

    private void stubReferences() {
        User user = new User();
        user.setId(1L);
        Category category = new Category();
        category.setId(2L);
        Ingredient ingredient = new Ingredient();
        ingredient.setId(3L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(ingredientRepository.findById(3L)).thenReturn(Optional.of(ingredient));
    }

    private RecipeCreateDto validRecipeRequest(String title) {
        RecipeStepCreateDto step = new RecipeStepCreateDto();
        step.setStepOrder(1);
        step.setDescription("Cook and serve");

        RecipeCreateDto request = new RecipeCreateDto();
        request.setTitle(title);
        request.setDescription("Recipe used in a bulk-operation unit test");
        request.setAuthorId(1L);
        request.setCategoryId(2L);
        request.setIngredientIds(Set.of(3L));
        request.setSteps(List.of(step));
        return request;
    }
}
