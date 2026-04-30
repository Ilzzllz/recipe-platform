package com.example.recipeplatform.service;

import com.example.recipeplatform.exception.TransactionDemoException;
import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.User;
import com.example.recipeplatform.repository.CategoryRepository;
import com.example.recipeplatform.repository.IngredientRepository;
import com.example.recipeplatform.repository.RecipeRepository;
import com.example.recipeplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeTransactionScenarioService {

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

    /**
     * БЕЗ @Transactional – каждый saveAndFlush() сразу пишет в БД.
     * Исключение выбрасывается после сохранения User, Category, Ingredient.
     * Результат: Эти 3 записи остаются в БД, рецепт не создаётся.
     */
    public void saveWithoutTransactional(String marker) {
        // 1. User
        User user = new User();
        user.setUsername(marker + "_author");
        user.setEmail(marker + "@lab.local");
        user.setBio("Created for transaction demo (no tx)");
        userRepository.saveAndFlush(user);

        // 2. Category
        Category category = new Category();
        category.setName(marker + "_category");
        category.setDescription("Created for transaction demo (no tx)");
        categoryRepository.saveAndFlush(category);

        // 3. Ingredient
        Ingredient ingredient = new Ingredient();
        ingredient.setName(marker + "_ingredient");
        ingredientRepository.saveAndFlush(ingredient);

        // Симулируем ошибку – рецепт не создаётся
        throw new TransactionDemoException("Without @Transactional", marker,
                "User, Category and Ingredient saved (partial commit), recipe NOT saved due to error.");
    }

    /**
     * С @Transactional – все операции внутри транзакции.
     * При выбросе исключения – полный откат, ничего не сохраняется.
     */
    @Transactional
    public void saveWithTransactional(String marker) {
        // 1. User
        User user = new User();
        user.setUsername(marker + "_author");
        user.setEmail(marker + "@lab.local");
        user.setBio("Created for transaction demo (with tx)");
        userRepository.save(user);

        // 2. Category
        Category category = new Category();
        category.setName(marker + "_category");
        category.setDescription("Created for transaction demo (with tx)");
        categoryRepository.save(category);

        // 3. Ingredient
        Ingredient ingredient = new Ingredient();
        ingredient.setName(marker + "_ingredient");
        ingredientRepository.save(ingredient);

        // 4. Recipe (для наглядности)
        Recipe recipe = new Recipe();
        recipe.setTitle(marker + "_recipe");
        recipe.setDescription("This recipe will be rolled back");
        recipe.setAuthor(user);
        recipe.setCategory(category);
        recipe.replaceIngredients(java.util.Set.of(ingredient));
        recipeRepository.save(recipe);

        throw new TransactionDemoException("With @Transactional", marker,
                "Everything rolled back due to intentional failure.");
    }
}