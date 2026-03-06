package com.example.recipeplatform.repository;

import com.example.recipeplatform.model.Recipe;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RecipeRepository {
    private final List<Recipe> recipes = List.of(
            new Recipe(1L, "Борщ", "Суп с капустой", "Любовь"),
            new Recipe(2L, "Плов", "Рис с мясом", "Зира")
    );

    public Recipe findById(Long id) {
        return recipes.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Recipe> findAll() {
        return recipes;
    }
}
