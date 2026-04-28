package com.example.recipeplatform.config;

import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.model.CookingStep;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.User;
import com.example.recipeplatform.repository.CategoryRepository;
import com.example.recipeplatform.repository.IngredientRepository;
import com.example.recipeplatform.repository.RecipeRepository;
import com.example.recipeplatform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashSet;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               IngredientRepository ingredientRepository,
                               RecipeRepository recipeRepository) {
        return args -> {
            if (recipeRepository.count() > 0) {
                return;
            }

            User anna = new User();
            anna.setUsername("anna");
            anna.setEmail("anna@recipes.local");
            anna.setBio("Home cook who shares family recipes");
            anna = userRepository.save(anna);

            User nikita = new User();
            nikita.setUsername("nikita");
            nikita.setEmail("nikita@recipes.local");
            nikita.setBio("Loves quick weeknight meals");
            nikita = userRepository.save(nikita);

            Category soups = new Category();
            soups.setName("Soups");
            soups.setDescription("Hot soups for everyday cooking");
            soups = categoryRepository.save(soups);

            Category desserts = new Category();
            desserts.setName("Desserts");
            desserts.setDescription("Sweet recipes for tea and coffee");
            desserts = categoryRepository.save(desserts);

            Ingredient beet = createIngredient("Beet", ingredientRepository);
            Ingredient potato = createIngredient("Potato", ingredientRepository);
            Ingredient sourCream = createIngredient("Sour cream", ingredientRepository);
            Ingredient mascarpone = createIngredient("Mascarpone", ingredientRepository);
            Ingredient coffee = createIngredient("Coffee", ingredientRepository);

            Recipe borscht = new Recipe();
            borscht.setTitle("Borscht");
            borscht.setDescription("Classic beet soup with vegetables and sour cream");
            borscht.setAuthor(anna);
            borscht.setCategory(soups);
            borscht.replaceIngredients(new LinkedHashSet<>(List.of(beet, potato, sourCream)));
            borscht.addStep(step(1, "Prepare the vegetables and broth base"));
            borscht.addStep(step(2, "Simmer until the vegetables are tender"));
            borscht.addStep(step(3, "Serve with a spoon of sour cream"));

            Recipe tiramisu = new Recipe();
            tiramisu.setTitle("Tiramisu");
            tiramisu.setDescription("Layered dessert with coffee and mascarpone");
            tiramisu.setAuthor(nikita);
            tiramisu.setCategory(desserts);
            tiramisu.replaceIngredients(new LinkedHashSet<>(List.of(mascarpone, coffee)));
            tiramisu.addStep(step(1, "Whisk the mascarpone cream"));
            tiramisu.addStep(step(2, "Soak biscuits in coffee"));
            tiramisu.addStep(step(3, "Layer and chill before serving"));

            recipeRepository.saveAll(List.of(borscht, tiramisu));
        };
    }

    private Ingredient createIngredient(String name, IngredientRepository ingredientRepository) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(name);
        return ingredientRepository.save(ingredient);
    }

    private CookingStep step(int order, String description) {
        CookingStep cookingStep = new CookingStep();
        cookingStep.setStepOrder(order);
        cookingStep.setDescription(description);
        return cookingStep;
    }
}
