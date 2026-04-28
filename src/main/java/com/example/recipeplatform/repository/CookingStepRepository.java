package com.example.recipeplatform.repository;

import com.example.recipeplatform.model.CookingStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CookingStepRepository extends JpaRepository<CookingStep, Long> {

    long countByDescriptionStartingWith(String prefix);
}
