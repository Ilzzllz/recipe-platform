package com.example.recipeplatform.repository;

import com.example.recipeplatform.model.Recipe;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class RecipeRepository {
    private final List<Recipe> recipes = List.of(
            new Recipe(1L, "Борщ", "Классический суп", "Супы"),
            new Recipe(2L, "Плов", "Узбекский плов с говядиной", "Горячее"),
            new Recipe(3L, "Тирамису", "Кофейный десерт", "Десерты")
    );

    public Optional<Recipe> findById(Long id) {
        return recipes.stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    public List<Recipe> findAll() {
        return recipes;
    }
}
