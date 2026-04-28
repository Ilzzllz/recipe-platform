package com.example.recipeplatform.repository;

import com.example.recipeplatform.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    long countByNameStartingWith(String prefix);
}
