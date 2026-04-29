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
import org.springframework.transaction.support.TransactionTemplate;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               IngredientRepository ingredientRepository,
                               RecipeRepository recipeRepository,
                               TransactionTemplate transactionTemplate) {
        return args -> transactionTemplate.executeWithoutResult(status -> {
            Map<String, User> users = new LinkedHashMap<>();
            users.put("anna", findOrCreateUser(userRepository, "anna", "anna@recipes.local",
                    "Home cook who shares family recipes"));
            users.put("nikita", findOrCreateUser(userRepository, "nikita", "nikita@recipes.local",
                    "Loves quick weeknight meals"));
            users.put("sofia", findOrCreateUser(userRepository, "sofia", "sofia@recipes.local",
                    "Collects cozy breakfast recipes and seasonal desserts"));
            users.put("max", findOrCreateUser(userRepository, "max", "max@recipes.local",
                    "Tests quick meals for busy student life"));
            users.put("demo_student", findOrCreateUser(userRepository, "demo_student",
                    "demo_student@recipes.local",
                    "Disposable lab profile. Safe to edit or delete before submission."));

            Map<String, Category> categories = new LinkedHashMap<>();
            categories.put("soups", findOrCreateCategory(categoryRepository, "Soups",
                    "Hot soups for everyday cooking"));
            categories.put("desserts", findOrCreateCategory(categoryRepository, "Desserts",
                    "Sweet recipes for tea and coffee"));
            categories.put("breakfasts", findOrCreateCategory(categoryRepository, "Breakfasts",
                    "Egg, toast and morning comfort food"));
            categories.put("salads", findOrCreateCategory(categoryRepository, "Salads",
                    "Fresh and warm salads for lunch and dinner"));
            categories.put("drafts", findOrCreateCategory(categoryRepository, "Lab Drafts",
                    "Temporary recipes for CRUD demos. Safe to delete before submission."));

            Map<String, Ingredient> ingredients = new LinkedHashMap<>();
            ingredients.put("beet", createIngredient("Beet", ingredientRepository));
            ingredients.put("potato", createIngredient("Potato", ingredientRepository));
            ingredients.put("sourCream", createIngredient("Sour cream", ingredientRepository));
            ingredients.put("mascarpone", createIngredient("Mascarpone", ingredientRepository));
            ingredients.put("coffee", createIngredient("Coffee", ingredientRepository));
            ingredients.put("egg", createIngredient("Egg", ingredientRepository));
            ingredients.put("tomato", createIngredient("Tomato", ingredientRepository));
            ingredients.put("bread", createIngredient("Bread", ingredientRepository));
            ingredients.put("feta", createIngredient("Feta", ingredientRepository));
            ingredients.put("cucumber", createIngredient("Cucumber", ingredientRepository));
            ingredients.put("chicken", createIngredient("Chicken fillet", ingredientRepository));
            ingredients.put("lettuce", createIngredient("Lettuce", ingredientRepository));
            ingredients.put("parmesan", createIngredient("Parmesan", ingredientRepository));
            ingredients.put("pumpkin", createIngredient("Pumpkin", ingredientRepository));
            ingredients.put("cream", createIngredient("Cream", ingredientRepository));
            ingredients.put("garlic", createIngredient("Garlic", ingredientRepository));
            ingredients.put("pasta", createIngredient("Pasta", ingredientRepository));
            ingredients.put("oliveOil", createIngredient("Olive oil", ingredientRepository));
            ingredients.put("banana", createIngredient("Banana", ingredientRepository));
            ingredients.put("milk", createIngredient("Milk", ingredientRepository));
            ingredients.put("honey", createIngredient("Honey", ingredientRepository));

            createRecipeIfMissing(
                    recipeRepository,
                    "Borscht",
                    "Classic beet soup with vegetables and sour cream",
                    users.get("anna"),
                    categories.get("soups"),
                    List.of(ingredients.get("beet"), ingredients.get("potato"), ingredients.get("sourCream")),
                    List.of(
                            "Prepare the vegetables and broth base",
                            "Simmer until the vegetables are tender",
                            "Serve with a spoon of sour cream"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Tiramisu",
                    "Layered dessert with coffee and mascarpone",
                    users.get("nikita"),
                    categories.get("desserts"),
                    List.of(ingredients.get("mascarpone"), ingredients.get("coffee"), ingredients.get("egg")),
                    List.of(
                            "Whisk the mascarpone cream",
                            "Soak biscuits in coffee",
                            "Layer and chill before serving"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Shakshuka",
                    "Eggs baked in a spicy tomato sauce for breakfast",
                    users.get("sofia"),
                    categories.get("breakfasts"),
                    List.of(ingredients.get("egg"), ingredients.get("tomato"), ingredients.get("garlic"), ingredients.get("oliveOil")),
                    List.of(
                            "Saute garlic in olive oil",
                            "Cook the tomato base until thickened",
                            "Crack eggs into the sauce and bake until set"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Greek Salad",
                    "Fresh salad with cucumber, tomato and feta",
                    users.get("anna"),
                    categories.get("salads"),
                    List.of(ingredients.get("tomato"), ingredients.get("cucumber"), ingredients.get("feta"), ingredients.get("oliveOil")),
                    List.of(
                            "Chop the vegetables into bite-size pieces",
                            "Add feta and drizzle with olive oil",
                            "Mix gently and serve immediately"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Caesar Chicken Salad",
                    "Warm chicken salad with lettuce and parmesan",
                    users.get("max"),
                    categories.get("salads"),
                    List.of(ingredients.get("chicken"), ingredients.get("lettuce"), ingredients.get("parmesan"), ingredients.get("bread")),
                    List.of(
                            "Pan-fry the chicken until golden",
                            "Toast the bread into crunchy croutons",
                            "Combine lettuce, chicken, parmesan and croutons"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Pumpkin Cream Soup",
                    "Smooth autumn soup with pumpkin and cream",
                    users.get("nikita"),
                    categories.get("soups"),
                    List.of(ingredients.get("pumpkin"), ingredients.get("cream"), ingredients.get("garlic"), ingredients.get("oliveOil")),
                    List.of(
                            "Roast the pumpkin with garlic",
                            "Blend with warm cream until smooth",
                            "Heat gently and season before serving"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "demo_lab_pasta",
                    "Temporary recipe for CRUD, delete and transactional demos. Safe to remove before submission.",
                    users.get("demo_student"),
                    categories.get("drafts"),
                    List.of(ingredients.get("pasta"), ingredients.get("garlic"), ingredients.get("oliveOil"), ingredients.get("parmesan")),
                    List.of(
                            "Boil the pasta until al dente",
                            "Warm garlic in olive oil without burning it",
                            "Mix pasta with oil and finish with parmesan"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "demo_lab_smoothie",
                    "Disposable breakfast sample for update and delete checks in Swagger.",
                    users.get("demo_student"),
                    categories.get("drafts"),
                    List.of(ingredients.get("banana"), ingredients.get("milk"), ingredients.get("honey")),
                    List.of(
                            "Slice the banana",
                            "Blend banana with milk",
                            "Add honey and blend again"
                    )
            );
        });
    }

    private Ingredient createIngredient(String name, IngredientRepository ingredientRepository) {
        return ingredientRepository.findAll().stream()
                .filter(ingredient -> ingredient.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setName(name);
                    return ingredientRepository.save(ingredient);
                });
    }

    private User findOrCreateUser(UserRepository userRepository, String username, String email, String bio) {
        return userRepository.findAll().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setBio(bio);
                    return userRepository.save(user);
                });
    }

    private Category findOrCreateCategory(CategoryRepository categoryRepository, String name, String description) {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(name);
                    category.setDescription(description);
                    return categoryRepository.save(category);
                });
    }

    private void createRecipeIfMissing(RecipeRepository recipeRepository,
                                       String title,
                                       String description,
                                       User author,
                                       Category category,
                                       List<Ingredient> ingredients,
                                       List<String> steps) {
        if (recipeRepository.existsByTitleIgnoreCase(title)) {
            return;
        }

        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setDescription(description);
        recipe.setAuthor(author);
        recipe.setCategory(category);
        recipe.replaceIngredients(new LinkedHashSet<>(ingredients));
        int order = 1;
        for (String stepDescription : steps) {
            recipe.addStep(step(order++, stepDescription));
        }
        recipeRepository.save(recipe);
    }

    private CookingStep step(int order, String description) {
        CookingStep cookingStep = new CookingStep();
        cookingStep.setStepOrder(order);
        cookingStep.setDescription(description);
        return cookingStep;
    }
}
