package com.example.recipeplatform.service;

import com.example.recipeplatform.cache.RecipeQueryCacheService;
import com.example.recipeplatform.dto.IngredientCreateDto;
import com.example.recipeplatform.dto.IngredientDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.mapper.IngredientMapper;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.RecipeIngredient;
import com.example.recipeplatform.repository.IngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private IngredientMapper ingredientMapper;

    @Mock
    private RecipeQueryCacheService recipeQueryCacheService;

    private IngredientService ingredientService;

    @BeforeEach
    void setUp() {
        ingredientService = new IngredientService(ingredientRepository, ingredientMapper, recipeQueryCacheService);
    }

    @Test
    @DisplayName("findAll should return sorted list of ingredient DTOs")
    void findAllShouldReturnSortedDtoList() {
        Ingredient ingredient = new Ingredient();
        IngredientDto dto = new IngredientDto();
        when(ingredientRepository.findAll(any(Sort.class))).thenReturn(List.of(ingredient));
        when(ingredientMapper.toDtoList(List.of(ingredient))).thenReturn(List.of(dto));

        List<IngredientDto> result = ingredientService.findAll();

        assertThat(result).containsExactly(dto);
        verify(ingredientRepository).findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    @DisplayName("getById should return ingredient DTO when found")
    void getByIdShouldReturnIngredientDto() {
        Ingredient ingredient = new Ingredient();
        IngredientDto dto = new IngredientDto();
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
        when(ingredientMapper.toDto(ingredient)).thenReturn(dto);

        IngredientDto result = ingredientService.getById(1L);

        assertThat(result).isSameAs(dto);
    }

    @Test
    @DisplayName("getById should throw NotFoundException when ingredient does not exist")
    void getByIdShouldThrowNotFoundException() {
        when(ingredientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredientService.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ingredient with id 999 was not found");
    }

    @Test
    @DisplayName("create should save ingredient and invalidate cache when name is unique")
    void createShouldSaveIngredientSuccessfully() {
        IngredientCreateDto request = sampleIngredientDto("Salt");
        Ingredient entity = new Ingredient();
        Ingredient saved = new Ingredient();
        IngredientDto dto = new IngredientDto();

        when(ingredientRepository.existsByNameIgnoreCase("Salt")).thenReturn(false);
        when(ingredientMapper.toEntity(request)).thenReturn(entity);
        when(ingredientRepository.save(entity)).thenReturn(saved);
        when(ingredientMapper.toDto(saved)).thenReturn(dto);

        IngredientDto result = ingredientService.create(request);

        assertThat(result).isSameAs(dto);
        verify(ingredientRepository).save(entity);
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("create should throw IllegalArgumentException when name already exists")
    void createShouldThrowWhenNameExists() {
        IngredientCreateDto request = sampleIngredientDto("Salt");
        when(ingredientRepository.existsByNameIgnoreCase("Salt")).thenReturn(true);

        assertThatThrownBy(() -> ingredientService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ingredient already exists");

        verify(ingredientRepository, never()).save(any());
    }

    @Test
    @DisplayName("update should modify ingredient when name is unique or unchanged")
    void updateShouldModifyIngredientSuccessfully() {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);
        IngredientCreateDto request = sampleIngredientDto("Sea Salt");
        IngredientDto dto = new IngredientDto();

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
        when(ingredientRepository.findByNameIgnoreCase("Sea Salt")).thenReturn(Optional.empty());
        when(ingredientRepository.save(ingredient)).thenReturn(ingredient);
        when(ingredientMapper.toDto(ingredient)).thenReturn(dto);

        IngredientDto result = ingredientService.update(1L, request);

        assertThat(result).isSameAs(dto);
        verify(ingredientMapper).updateEntity(ingredient, request);
        verify(ingredientRepository).save(ingredient);
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("update should allow keeping same name for same ingredient id")
    void updateShouldAllowSameNameForSameId() {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);
        IngredientCreateDto request = sampleIngredientDto("Salt");

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
        when(ingredientRepository.findByNameIgnoreCase("Salt")).thenReturn(Optional.of(ingredient));
        when(ingredientRepository.save(ingredient)).thenReturn(ingredient);
        when(ingredientMapper.toDto(ingredient)).thenReturn(new IngredientDto());

        IngredientDto result = ingredientService.update(1L, request);

        assertThat(result).isNotNull();
        verify(ingredientRepository).save(ingredient);
    }

    @Test
    @DisplayName("update should throw IllegalArgumentException when name belongs to another ingredient")
    void updateShouldThrowWhenNameBelongsToAnotherIngredient() {
        Ingredient current = new Ingredient();
        current.setId(1L);
        Ingredient other = new Ingredient();
        other.setId(2L);
        IngredientCreateDto request = sampleIngredientDto("Existing Salt");

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(current));
        when(ingredientRepository.findByNameIgnoreCase("Existing Salt")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> ingredientService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ingredient already exists");

        verify(ingredientRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete should unbind from recipes, delete from repo and invalidate cache")
    void deleteShouldUnbindAndRemoveIngredient() {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);

        Recipe recipe = new Recipe();
        recipe.setIngredients(new LinkedHashSet<>(Set.of(ingredient)));
        ingredient.setRecipes(new LinkedHashSet<>(Set.of(recipe)));

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));

        ingredientService.delete(1L);

        assertThat(recipe.getIngredients()).doesNotContain(ingredient);
        assertThat(ingredient.getRecipes()).isEmpty();
        verify(ingredientRepository).delete(ingredient);
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("delete should also detach quantity rows from recipes, skipping rows without a recipe")
    void deleteShouldDetachRecipeIngredientDetails() {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);
        Ingredient other = new Ingredient();
        other.setId(2L);

        Recipe recipe = new Recipe();
        RecipeIngredient ownDetail = new RecipeIngredient();
        ownDetail.setIngredient(ingredient);
        ownDetail.setRecipe(recipe);
        RecipeIngredient otherDetail = new RecipeIngredient();
        otherDetail.setIngredient(other);
        otherDetail.setRecipe(recipe);
        recipe.setIngredients(new LinkedHashSet<>(Set.of(ingredient, other)));
        recipe.setRecipeIngredientDetails(new java.util.ArrayList<>(java.util.List.of(ownDetail, otherDetail)));

        Recipe detachedOnlyRecipe = new Recipe();
        RecipeIngredient detailOfUnlinkedRecipe = new RecipeIngredient();
        detailOfUnlinkedRecipe.setIngredient(ingredient);
        detailOfUnlinkedRecipe.setRecipe(detachedOnlyRecipe);
        detachedOnlyRecipe.setIngredients(new LinkedHashSet<>(Set.of(ingredient)));
        detachedOnlyRecipe.setRecipeIngredientDetails(new java.util.ArrayList<>(java.util.List.of(detailOfUnlinkedRecipe)));
        RecipeIngredient orphanDetail = new RecipeIngredient();
        orphanDetail.setIngredient(ingredient);

        ingredient.setRecipes(new LinkedHashSet<>(Set.of(recipe)));
        ingredient.setRecipeIngredientDetails(new java.util.ArrayList<>(
                java.util.List.of(ownDetail, detailOfUnlinkedRecipe, orphanDetail)));
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));

        ingredientService.delete(1L);

        assertThat(recipe.getIngredients()).containsExactly(other);
        assertThat(recipe.getRecipeIngredientDetails()).containsExactly(otherDetail);
        assertThat(detachedOnlyRecipe.getIngredients()).isEmpty();
        assertThat(detachedOnlyRecipe.getRecipeIngredientDetails()).isEmpty();
        assertThat(ingredient.getRecipeIngredientDetails()).isEmpty();
        verify(ingredientRepository).delete(ingredient);
    }

    private IngredientCreateDto sampleIngredientDto(String name) {
        IngredientCreateDto dto = new IngredientCreateDto();
        dto.setName(name);
        return dto;
    }
}
