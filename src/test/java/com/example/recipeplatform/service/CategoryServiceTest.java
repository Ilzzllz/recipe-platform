package com.example.recipeplatform.service;

import com.example.recipeplatform.cache.RecipeQueryCacheService;
import com.example.recipeplatform.dto.CategoryCreateDto;
import com.example.recipeplatform.dto.CategoryDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.mapper.CategoryMapper;
import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.repository.CategoryRepository;
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
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private RecipeQueryCacheService recipeQueryCacheService;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, categoryMapper, recipeQueryCacheService);
    }

    @Test
    @DisplayName("findAll should return sorted list of category DTOs")
    void findAllShouldReturnSortedDtoList() {
        Category category = new Category();
        CategoryDto dto = new CategoryDto();
        when(categoryRepository.findAll(any(Sort.class))).thenReturn(List.of(category));
        when(categoryMapper.toDtoList(List.of(category))).thenReturn(List.of(dto));

        List<CategoryDto> result = categoryService.findAll();

        assertThat(result).containsExactly(dto);
        verify(categoryRepository).findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    @DisplayName("getById should return category DTO when found")
    void getByIdShouldReturnCategoryDto() {
        Category category = new Category();
        CategoryDto dto = new CategoryDto();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(dto);

        CategoryDto result = categoryService.getById(1L);

        assertThat(result).isSameAs(dto);
    }

    @Test
    @DisplayName("getById should throw NotFoundException when category does not exist")
    void getByIdShouldThrowNotFoundException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category with id 999 was not found");
    }

    @Test
    @DisplayName("create should save category and invalidate cache when name is unique")
    void createShouldSaveCategorySuccessfully() {
        CategoryCreateDto request = sampleCategoryDto("Soups");
        Category entity = new Category();
        Category saved = new Category();
        CategoryDto dto = new CategoryDto();

        when(categoryRepository.existsByNameIgnoreCase("Soups")).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(entity);
        when(categoryRepository.save(entity)).thenReturn(saved);
        when(categoryMapper.toDto(saved)).thenReturn(dto);

        CategoryDto result = categoryService.create(request);

        assertThat(result).isSameAs(dto);
        verify(categoryRepository).save(entity);
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("create should throw IllegalArgumentException when name already exists")
    void createShouldThrowWhenNameExists() {
        CategoryCreateDto request = sampleCategoryDto("Soups");
        when(categoryRepository.existsByNameIgnoreCase("Soups")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category already exists");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("update should modify category when name is unique or unchanged")
    void updateShouldModifyCategorySuccessfully() {
        Category category = new Category();
        category.setId(1L);
        CategoryCreateDto request = sampleCategoryDto("Hot Soups");
        CategoryDto dto = new CategoryDto();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase("Hot Soups")).thenReturn(Optional.empty());
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(dto);

        CategoryDto result = categoryService.update(1L, request);

        assertThat(result).isSameAs(dto);
        verify(categoryMapper).updateEntity(category, request);
        verify(categoryRepository).save(category);
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("update should allow keeping same name for same category id")
    void updateShouldAllowSameNameForSameId() {
        Category category = new Category();
        category.setId(1L);
        CategoryCreateDto request = sampleCategoryDto("Soups");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase("Soups")).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(new CategoryDto());

        CategoryDto result = categoryService.update(1L, request);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("update should throw IllegalArgumentException when name belongs to another category")
    void updateShouldThrowWhenNameBelongsToAnotherCategory() {
        Category current = new Category();
        current.setId(1L);
        Category other = new Category();
        other.setId(2L);
        CategoryCreateDto request = sampleCategoryDto("Existing Name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(current));
        when(categoryRepository.findByNameIgnoreCase("Existing Name")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> categoryService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category already exists");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete should remove category without recipes and invalidate cache")
    void deleteShouldRemoveCategoryWithoutRecipes() {
        Category category = new Category();
        category.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.delete(1L);

        verify(categoryRepository).delete(category);
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("delete should throw IllegalArgumentException when category has recipes")
    void deleteShouldThrowWhenCategoryHasRecipes() {
        Category category = new Category();
        category.setId(1L);
        category.getRecipes().add(new Recipe());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot delete category that has recipes. Remove or reassign recipes first.");

        verify(categoryRepository, never()).delete(any());
    }

    private CategoryCreateDto sampleCategoryDto(String name) {
        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setName(name);
        dto.setDescription("Category description test");
        return dto;
    }
}
