package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.CategoryCreateDto;
import com.example.recipeplatform.dto.CategoryDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.mapper.CategoryMapper;
import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private static final String CATEGORY_WITH_ID_PREFIX = "Category with id ";
    private static final String NOT_FOUND_SUFFIX = " was not found";
    private static final String CATEGORY_ALREADY_EXISTS = "Category already exists";
    private static final String CATEGORY_NOT_EMPTY = "Cannot delete category that has recipes. Remove or reassign recipes first.";
    private static final Sort SORT_BY_ID = Sort.by(Sort.Direction.ASC, "id");

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        return categoryMapper.toDtoList(categoryRepository.findAll(SORT_BY_ID));
    }

    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        return categoryMapper.toDto(findEntity(id));
    }

    @Transactional
    public CategoryDto create(CategoryCreateDto dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException(CATEGORY_ALREADY_EXISTS);
        }
        return categoryMapper.toDto(categoryRepository.save(categoryMapper.toEntity(dto)));
    }

    @Transactional
    public CategoryDto update(Long id, CategoryCreateDto dto) {
        Category category = findEntity(id);
        categoryRepository.findByNameIgnoreCase(dto.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(CATEGORY_ALREADY_EXISTS);
                });
        categoryMapper.updateEntity(category, dto);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = findEntity(id);
        if (!category.getRecipes().isEmpty()) {
            throw new IllegalArgumentException(CATEGORY_NOT_EMPTY);
        }
        categoryRepository.delete(category);
    }

    private Category findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
    }
}