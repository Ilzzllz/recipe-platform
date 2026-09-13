package com.example.recipeplatform.service;

import com.example.recipeplatform.cache.RecipeQueryCacheService;
import com.example.recipeplatform.dto.CookingStepCreateDto;
import com.example.recipeplatform.dto.CookingStepDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.mapper.CookingStepMapper;
import com.example.recipeplatform.model.CookingStep;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.repository.CookingStepRepository;
import com.example.recipeplatform.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CookingStepServiceTest {

    @Mock
    private CookingStepRepository cookingStepRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private CookingStepMapper cookingStepMapper;

    @Mock
    private RecipeQueryCacheService recipeQueryCacheService;

    private CookingStepService cookingStepService;

    @BeforeEach
    void setUp() {
        cookingStepService = new CookingStepService(cookingStepRepository, recipeRepository,
                cookingStepMapper, recipeQueryCacheService);
    }

    @Test
    @DisplayName("findAll should return sorted list of cooking step DTOs")
    void findAllShouldReturnSortedList() {
        CookingStep step = new CookingStep();
        CookingStepDto dto = new CookingStepDto();
        when(cookingStepRepository.findAll(any(Sort.class))).thenReturn(List.of(step));
        when(cookingStepMapper.toDtoList(List.of(step))).thenReturn(List.of(dto));

        List<CookingStepDto> result = cookingStepService.findAll();

        assertThat(result).containsExactly(dto);
        verify(cookingStepRepository).findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    @DisplayName("findById should return step DTO when step exists")
    void findByIdShouldReturnStepDto() {
        CookingStep step = new CookingStep();
        CookingStepDto dto = new CookingStepDto();
        when(cookingStepRepository.findById(10L)).thenReturn(Optional.of(step));
        when(cookingStepMapper.toDto(step)).thenReturn(dto);

        CookingStepDto result = cookingStepService.findById(10L);

        assertThat(result).isSameAs(dto);
    }

    @Test
    @DisplayName("findById should throw NotFoundException when step does not exist")
    void findByIdShouldThrowNotFoundException() {
        when(cookingStepRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cookingStepService.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Cooking step with id 999 was not found");
    }

    @Test
    @DisplayName("create should save cooking step and invalidate cache")
    void createShouldSaveCookingStepSuccessfully() {
        CookingStepCreateDto request = sampleStepDto(1L, 1, "Chop onions");
        Recipe recipe = new Recipe();
        CookingStep step = new CookingStep();
        CookingStep saved = new CookingStep();
        CookingStepDto dto = new CookingStepDto();

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(cookingStepMapper.toEntity(request)).thenReturn(step);
        when(cookingStepRepository.save(step)).thenReturn(saved);
        when(cookingStepMapper.toDto(saved)).thenReturn(dto);

        CookingStepDto result = cookingStepService.create(request);

        assertThat(result).isSameAs(dto);
        assertThat(step.getRecipe()).isSameAs(recipe);
        verify(cookingStepRepository).save(step);
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("create should throw IllegalArgumentException when recipeId is null")
    void createShouldThrowWhenRecipeIdIsNull() {
        CookingStepCreateDto request = sampleStepDto(null, 1, "Chop onions");

        assertThatThrownBy(() -> cookingStepService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("recipeId is required for cooking step requests");

        verify(cookingStepRepository, never()).save(any());
    }

    @Test
    @DisplayName("create should throw NotFoundException when referenced recipe does not exist")
    void createShouldThrowWhenRecipeNotFound() {
        CookingStepCreateDto request = sampleStepDto(999L, 1, "Chop onions");
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cookingStepService.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Recipe with id 999 was not found");

        verify(cookingStepRepository, never()).save(any());
    }

    @Test
    @DisplayName("update should modify cooking step and invalidate cache")
    void updateShouldModifyStepSuccessfully() {
        CookingStepCreateDto request = sampleStepDto(1L, 2, "Boil broth");
        CookingStep step = new CookingStep();
        Recipe recipe = new Recipe();
        CookingStepDto dto = new CookingStepDto();

        when(cookingStepRepository.findById(10L)).thenReturn(Optional.of(step));
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(cookingStepRepository.save(step)).thenReturn(step);
        when(cookingStepMapper.toDto(step)).thenReturn(dto);

        CookingStepDto result = cookingStepService.update(10L, request);

        assertThat(result).isSameAs(dto);
        verify(cookingStepMapper).updateEntity(step, request);
        assertThat(step.getRecipe()).isSameAs(recipe);
        verify(cookingStepRepository).save(step);
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("update should throw NotFoundException when step does not exist")
    void updateShouldThrowWhenStepNotFound() {
        CookingStepCreateDto request = sampleStepDto(1L, 2, "Boil broth");
        when(cookingStepRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cookingStepService.update(999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Cooking step with id 999 was not found");

        verify(cookingStepRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete should remove existing step and invalidate cache")
    void deleteShouldRemoveStepSuccessfully() {
        CookingStep step = new CookingStep();
        when(cookingStepRepository.findById(10L)).thenReturn(Optional.of(step));

        cookingStepService.delete(10L);

        verify(cookingStepRepository).delete(step);
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("delete should throw NotFoundException when step does not exist")
    void deleteShouldThrowWhenStepNotFound() {
        when(cookingStepRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cookingStepService.delete(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Cooking step with id 999 was not found");

        verify(cookingStepRepository, never()).delete(any());
    }

    private CookingStepCreateDto sampleStepDto(Long recipeId, int order, String desc) {
        CookingStepCreateDto dto = new CookingStepCreateDto();
        dto.setRecipeId(recipeId);
        dto.setStepOrder(order);
        dto.setDescription(desc);
        return dto;
    }
}
