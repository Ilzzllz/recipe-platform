package com.example.recipeplatform.service;

import com.example.recipeplatform.cache.RecipeQueryCacheService;
import com.example.recipeplatform.dto.RecipeCreateDto;
import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.dto.RecipeStepCreateDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.mapper.CookingStepMapper;
import com.example.recipeplatform.mapper.RecipeMapper;
import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.User;
import com.example.recipeplatform.repository.CategoryRepository;
import com.example.recipeplatform.repository.IngredientRepository;
import com.example.recipeplatform.repository.RecipeRepository;
import com.example.recipeplatform.repository.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private RecipeQueryCacheService recipeQueryCacheService;

    private RecipeService recipeService;

    @BeforeEach
    void setUp() {
        recipeService = new RecipeService(recipeRepository, userRepository, categoryRepository,
                ingredientRepository, recipeMapper, cookingStepMapper, entityManagerFactory,
                recipeQueryCacheService);
    }

    @Test
    void createBulkShouldSaveEveryRecipeAndInvalidateQueryCache() {
        stubReferences();
        when(recipeRepository.saveAndFlush(any(Recipe.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(recipeMapper.toDto(any(Recipe.class))).thenReturn(new RecipeDto());

        List<RecipeDto> result = recipeService.createBulk(List.of(request("First soup"), request("Second soup")));

        ArgumentCaptor<Recipe> recipes = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository, times(2)).saveAndFlush(recipes.capture());
        verify(recipeQueryCacheService).invalidateAll();
        assertThat(result).hasSize(2);
        assertThat(recipes.getAllValues()).extracting(Recipe::getTitle)
                .containsExactly("First soup", "Second soup");
    }

    @Test
    void createBulkWithoutTransactionShouldKeepFirstSaveAttemptWhenSecondRecipeIsInvalid() {
        stubReferences();
        when(recipeRepository.saveAndFlush(any(Recipe.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RecipeCreateDto invalid = request("Broken soup");
        invalid.setIngredientIds(Set.of(999L));
        when(ingredientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.createBulkWithoutTransaction(List.of(request("Saved soup"), invalid)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ingredient with id 999");

        verify(recipeRepository).saveAndFlush(any(Recipe.class));
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    void createBulkShouldStopBeforeSavingWhenOptionalAuthorIsEmpty() {
        RecipeCreateDto request = request("No author soup");
        when(userRepository.findById(eq(1L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.createBulk(List.of(request)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with id 1");

        verify(recipeRepository, never()).saveAndFlush(any(Recipe.class));
        verify(recipeQueryCacheService).invalidateAll();
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

    private RecipeCreateDto request(String title) {
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
