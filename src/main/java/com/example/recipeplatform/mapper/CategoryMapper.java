package com.example.recipeplatform.mapper;

import com.example.recipeplatform.dto.CategoryCreateDto;
import com.example.recipeplatform.dto.CategoryDto;
import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.util.TextNormalizer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper {

    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }

    public Category toEntity(CategoryCreateDto dto) {
        Category category = new Category();
        updateEntity(category, dto);
        return category;
    }

    public void updateEntity(Category category, CategoryCreateDto dto) {
        category.setName(TextNormalizer.normalize(dto.getName()));
        category.setDescription(TextNormalizer.normalize(dto.getDescription()));
    }

    public List<CategoryDto> toDtoList(List<Category> categories) {
        return categories.stream()
                .map(this::toDto)
                .toList();
    }
}
