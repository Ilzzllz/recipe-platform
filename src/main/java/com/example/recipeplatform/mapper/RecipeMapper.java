package com.example.recipeplatform.mapper;

import com.example.recipeplatform.dto.AuthorReferenceDto;
import com.example.recipeplatform.dto.CategoryReferenceDto;
import com.example.recipeplatform.dto.RecipeCreateDto;
import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.dto.NutritionSummaryDto;
import com.example.recipeplatform.dto.RecipeIngredientDto;
import com.example.recipeplatform.model.CookingStep;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.RecipeIngredient;
import com.example.recipeplatform.util.TextNormalizer;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class RecipeMapper {

    private final IngredientMapper ingredientMapper;
    private final CookingStepMapper cookingStepMapper;

    public RecipeMapper(IngredientMapper ingredientMapper, CookingStepMapper cookingStepMapper) {
        this.ingredientMapper = ingredientMapper;
        this.cookingStepMapper = cookingStepMapper;
    }

    public RecipeDto toDto(Recipe recipe) {
        if (recipe == null) {
            return null;
        }

        RecipeDto dto = new RecipeDto();
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        dto.setPortions(recipe.getPortions() == null || recipe.getPortions() < 1 ? 1 : recipe.getPortions());

        AuthorReferenceDto authorRef = new AuthorReferenceDto();
        authorRef.setId(recipe.getAuthor().getId());
        authorRef.setUsername(recipe.getAuthor().getUsername());
        dto.setAuthor(authorRef);

        CategoryReferenceDto categoryRef = new CategoryReferenceDto();
        categoryRef.setId(recipe.getCategory().getId());
        categoryRef.setName(recipe.getCategory().getName());
        dto.setCategory(categoryRef);

        dto.setIngredients(uniqueIngredients(recipe));
        dto.setRecipeIngredients(recipeIngredientDtos(recipe));
        dto.setSteps(uniqueSteps(recipe));
        dto.setNutrition(calculateNutrition(recipe));
        return dto;
    }

    public Recipe toEntity(RecipeCreateDto dto) {
        Recipe recipe = new Recipe();
        updateEntity(recipe, dto);
        return recipe;
    }

    public void updateEntity(Recipe recipe, RecipeCreateDto dto) {
        recipe.setTitle(TextNormalizer.normalize(dto.getTitle()));
        recipe.setDescription(TextNormalizer.normalize(dto.getDescription()));
        recipe.setPortions(dto.getPortions() == null || dto.getPortions() < 1 ? 1 : dto.getPortions());
    }

    public List<RecipeDto> toDtoList(List<Recipe> recipes) {
        return recipes.stream()
                .map(this::toDto)
                .toList();
    }

    private List<com.example.recipeplatform.dto.IngredientDto> uniqueIngredients(Recipe recipe) {
        Map<Long, Ingredient> uniqueIngredients = new LinkedHashMap<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            uniqueIngredients.putIfAbsent(ingredient.getId(), ingredient);
        }
        return uniqueIngredients.values().stream()
                .map(ingredientMapper::toDto)
                .toList();
    }

    private List<RecipeIngredientDto> recipeIngredientDtos(Recipe recipe) {
        if (recipe.getRecipeIngredientDetails() != null && !recipe.getRecipeIngredientDetails().isEmpty()) {
            return recipe.getRecipeIngredientDetails().stream().map(detail -> {
                RecipeIngredientDto dto = new RecipeIngredientDto();
                dto.setIngredient(ingredientMapper.toDto(detail.getIngredient()));
                dto.setQuantity(detail.getQuantity());
                dto.setUnit(detail.getUnit());
                return dto;
            }).toList();
        }
        return recipe.getIngredients().stream().map(ingredient -> {
            RecipeIngredientDto dto = new RecipeIngredientDto();
            dto.setIngredient(ingredientMapper.toDto(ingredient));
            dto.setQuantity(BigDecimal.valueOf(100));
            dto.setUnit("г");
            return dto;
        }).toList();
    }

    private NutritionSummaryDto calculateNutrition(Recipe recipe) {
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal calories = BigDecimal.ZERO;
        BigDecimal proteins = BigDecimal.ZERO;
        BigDecimal fats = BigDecimal.ZERO;
        BigDecimal carbohydrates = BigDecimal.ZERO;

        if (recipe.getRecipeIngredientDetails() != null && !recipe.getRecipeIngredientDetails().isEmpty()) {
            for (RecipeIngredient detail : recipe.getRecipeIngredientDetails()) {
                BigDecimal grams = grams(detail);
                BigDecimal factor = grams.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
                totalWeight = totalWeight.add(grams);
                calories = calories.add(value(detail.getIngredient().getCaloriesPer100g()).multiply(factor));
                proteins = proteins.add(value(detail.getIngredient().getProteinsPer100g()).multiply(factor));
                fats = fats.add(value(detail.getIngredient().getFatsPer100g()).multiply(factor));
                carbohydrates = carbohydrates.add(value(detail.getIngredient().getCarbohydratesPer100g()).multiply(factor));
            }
        } else {
            for (Ingredient ingredient : recipe.getIngredients()) {
                totalWeight = totalWeight.add(BigDecimal.valueOf(100));
                calories = calories.add(value(ingredient.getCaloriesPer100g()));
                proteins = proteins.add(value(ingredient.getProteinsPer100g()));
                fats = fats.add(value(ingredient.getFatsPer100g()));
                carbohydrates = carbohydrates.add(value(ingredient.getCarbohydratesPer100g()));
            }
        }

        NutritionSummaryDto dto = new NutritionSummaryDto();
        dto.setTotalWeightGrams(round(totalWeight));
        dto.setCaloriesKcal(round(calories));
        dto.setProteinsGrams(round(proteins));
        dto.setFatsGrams(round(fats));
        dto.setCarbohydratesGrams(round(carbohydrates));
        BigDecimal denominator = totalWeight.signum() == 0 ? BigDecimal.ONE : totalWeight;
        dto.setCaloriesPer100g(round(calories.multiply(BigDecimal.valueOf(100)).divide(denominator, 8, RoundingMode.HALF_UP)));
        dto.setProteinsPer100g(round(proteins.multiply(BigDecimal.valueOf(100)).divide(denominator, 8, RoundingMode.HALF_UP)));
        dto.setFatsPer100g(round(fats.multiply(BigDecimal.valueOf(100)).divide(denominator, 8, RoundingMode.HALF_UP)));
        dto.setCarbohydratesPer100g(round(carbohydrates.multiply(BigDecimal.valueOf(100)).divide(denominator, 8, RoundingMode.HALF_UP)));
        int portions = recipe.getPortions() == null || recipe.getPortions() < 1 ? 1 : recipe.getPortions();
        dto.setPortions(portions);
        dto.setCaloriesPerPortion(round(calories.divide(BigDecimal.valueOf(portions), 8, RoundingMode.HALF_UP)));
        dto.setProteinsPerPortion(round(proteins.divide(BigDecimal.valueOf(portions), 8, RoundingMode.HALF_UP)));
        dto.setFatsPerPortion(round(fats.divide(BigDecimal.valueOf(portions), 8, RoundingMode.HALF_UP)));
        dto.setCarbohydratesPerPortion(round(carbohydrates.divide(BigDecimal.valueOf(portions), 8, RoundingMode.HALF_UP)));
        return dto;
    }

    private BigDecimal grams(RecipeIngredient detail) {
        BigDecimal quantity = value(detail.getQuantity());
        String unit = detail.getUnit() == null ? "г" : detail.getUnit().trim().toLowerCase();
        if (unit.equals("шт") || unit.equals("шт.") || unit.equals("pcs")) {
            return quantity.multiply(value(detail.getIngredient().getGramsPerUnit()));
        }
        return quantity;
    }

    private BigDecimal value(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    private List<com.example.recipeplatform.dto.CookingStepDto> uniqueSteps(Recipe recipe) {
        Map<Long, CookingStep> uniqueSteps = new LinkedHashMap<>();
        for (CookingStep step : recipe.getSteps()) {
            uniqueSteps.putIfAbsent(step.getId(), step);
        }
        return uniqueSteps.values().stream()
                .sorted(Comparator.comparing(CookingStep::getStepOrder))
                .map(cookingStepMapper::toDto)
                .toList();
    }
}
