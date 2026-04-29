package com.example.recipeplatform.repository;

import com.example.recipeplatform.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Ingredient> findByNameIgnoreCase(String name);

    long countByNameStartingWith(String prefix);
}
