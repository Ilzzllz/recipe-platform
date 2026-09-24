package com.example.recipeplatform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal caloriesPer100g = java.math.BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal proteinsPer100g = java.math.BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal fatsPer100g = java.math.BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal carbohydratesPer100g = java.math.BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal gramsPerUnit = java.math.BigDecimal.ONE;

    @ManyToMany(mappedBy = "ingredients")
    private Set<Recipe> recipes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "ingredient")
    private List<RecipeIngredient> recipeIngredientDetails = new ArrayList<>();
}
