package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.mapper.RecipeMapper;
import com.example.recipeplatform.repository.RecipeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
public class RecipeService {
    private final RecipeRepository repository;
    private final RecipeMapper mapper;

    public RecipeDto getById(Long id) {
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    public List<RecipeDto> searchByTitle(String title) {
        return repository.findAll().stream()
                .filter(r -> r.getTitle().equalsIgnoreCase(title))
                .map(mapper::toDto)
                .toList();
    }
}
