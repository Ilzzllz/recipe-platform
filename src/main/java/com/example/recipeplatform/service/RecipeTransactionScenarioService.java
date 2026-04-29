package com.example.recipeplatform.service;

import com.example.recipeplatform.exception.TransactionDemoException;
import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.model.CookingStep;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.User;
import com.example.recipeplatform.repository.CategoryRepository;
import com.example.recipeplatform.repository.IngredientRepository;
import com.example.recipeplatform.repository.RecipeRepository;
import com.example.recipeplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

@Service
public class RecipeTransactionScenarioService {

    private static final String FAILURE_MESSAGE = "Intentional failure after saving related entities";

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;

    public RecipeTransactionScenarioService(UserRepository userRepository,
                                            CategoryRepository categoryRepository,
                                            IngredientRepository ingredientRepository,
                                            RecipeRepository recipeRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
    }

    public void saveWithoutTransactional(String marker) {
        persistGraphAndFail("Without @Transactional", marker);
    }

    @Transactional
    public void saveWithTransactional(String marker) {
        persistGraphAndFail("With @Transactional", marker);
    }

    private void persistGraphAndFail(String scenario, String marker) {
        User user = new User();
        user.setUsername(marker + "_author");
        user.setEmail(marker + "@lab.local");
        user.setBio("Created for transaction demo");
        user = userRepository.save(user);

        Category category = new Category();
        category.setName(marker + "_category");
        category.setDescription("Created for transaction demo");
        category = categoryRepository.save(category);

        Ingredient ingredient = new Ingredient();
        ingredient.setName(marker + "_ingredient");
        ingredient = ingredientRepository.save(ingredient);

        Recipe recipe = new Recipe();
        recipe.setTitle(marker + "_recipe");
        recipe.setDescription("Recipe created inside the transaction demo");
        recipe.setAuthor(user);
        recipe.setCategory(category);
        recipe.replaceIngredients(new LinkedHashSet<>(List.of(ingredient)));

        CookingStep firstStep = new CookingStep();
        firstStep.setStepOrder(1);
        firstStep.setDescription(marker + "_step_prepare");
        recipe.addStep(firstStep);

        CookingStep secondStep = new CookingStep();
        secondStep.setStepOrder(2);
        secondStep.setDescription(marker + "_step_fail");
        recipe.addStep(secondStep);

        recipeRepository.save(recipe);

        throw new TransactionDemoException(scenario, marker, FAILURE_MESSAGE);
    }
}
