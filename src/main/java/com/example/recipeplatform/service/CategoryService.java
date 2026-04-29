package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.CategoryCreateDto;
import com.example.recipeplatform.dto.CategoryDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.mapper.CategoryMapper;
import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private static final String CATEGORY_WITH_ID_PREFIX = "Category with id ";
    private static final String NOT_FOUND_SUFFIX = " was not found";

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        return categoryMapper.toDto(findEntity(id));
    }

    @Transactional
    public CategoryDto create(CategoryCreateDto dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Category already exists");
        }
        return categoryMapper.toDto(categoryRepository.save(categoryMapper.toEntity(dto)));
    }

    @Transactional
    public CategoryDto update(Long id, CategoryCreateDto dto) {
        Category category = findEntity(id);
        categoryMapper.updateEntity(category, dto);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.delete(findEntity(id));
    }

    private Category findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
    }
}
