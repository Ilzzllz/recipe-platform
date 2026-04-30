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

    private static final String USER_ANNA = "anna";
    private static final String USER_NIKITA = "nikita";
    private static final String USER_SOFIA = "sofia";
    private static final String USER_MAX = "max";
    private static final String USER_DEMO_STUDENT = "demo_student";

    private static final String CATEGORY_SOUPS = "soups";
    private static final String CATEGORY_DESSERTS = "desserts";
    private static final String CATEGORY_BREAKFASTS = "breakfasts";
    private static final String CATEGORY_SALADS = "salads";
    private static final String CATEGORY_DRAFTS = "drafts";
    private static final String CATEGORY_GRILL = "grill";
    private static final String CATEGORY_VEGAN = "vegan";

    private static final String INGREDIENT_BEET = "beet";
    private static final String INGREDIENT_POTATO = "potato";
    private static final String INGREDIENT_SOUR_CREAM = "sourCream";
    private static final String INGREDIENT_MASCARPONE = "mascarpone";
    private static final String INGREDIENT_COFFEE = "coffee";
    private static final String INGREDIENT_EGG = "egg";
    private static final String INGREDIENT_TOMATO = "tomato";
    private static final String INGREDIENT_BREAD = "bread";
    private static final String INGREDIENT_FETA = "feta";
    private static final String INGREDIENT_CUCUMBER = "cucumber";
    private static final String INGREDIENT_CHICKEN = "chicken";
    private static final String INGREDIENT_LETTUCE = "lettuce";
    private static final String INGREDIENT_PARMESAN = "parmesan";
    private static final String INGREDIENT_PUMPKIN = "pumpkin";
    private static final String INGREDIENT_CREAM = "cream";
    private static final String INGREDIENT_GARLIC = "garlic";
    private static final String INGREDIENT_PASTA = "pasta";
    private static final String INGREDIENT_OLIVE_OIL = "oliveOil";
    private static final String INGREDIENT_BANANA = "banana";
    private static final String INGREDIENT_MILK = "milk";
    private static final String INGREDIENT_HONEY = "honey";
    private static final String INGREDIENT_FLOUR = "flour";
    private static final String INGREDIENT_SUGAR = "sugar";
    private static final String INGREDIENT_BUTTER = "butter";
    private static final String INGREDIENT_SHRIMP = "shrimp";
    private static final String INGREDIENT_AVOCADO = "avocado";
    private static final String INGREDIENT_DARK_CHOCOLATE = "darkChocolate";
    private static final String INGREDIENT_NOODLES = "noodles";
    private static final String INGREDIENT_CARROT = "carrot";
    private static final String INGREDIENT_ONION = "onion";
    private static final String INGREDIENT_BERRIES = "berries";
    private static final String INGREDIENT_YOGURT = "yogurt";
    private static final String INGREDIENT_TUNA = "tuna";
    private static final String INGREDIENT_ZUCCHINI = "zucchini";
    private static final String INGREDIENT_PEPPER = "pepper";
    private static final String INGREDIENT_LEMON = "lemon";   // <-- добавлено

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               IngredientRepository ingredientRepository,
                               RecipeRepository recipeRepository,
                               TransactionTemplate transactionTemplate) {
        return args -> transactionTemplate.executeWithoutResult(status -> {
            Map<String, User> users = new LinkedHashMap<>();
            users.put(USER_ANNA, findOrCreateUser(userRepository, USER_ANNA, "anna@recipes.local",
                    "Home cook who shares family recipes"));
            users.put(USER_NIKITA, findOrCreateUser(userRepository, USER_NIKITA, "nikita@recipes.local",
                    "Loves quick weeknight meals"));
            users.put(USER_SOFIA, findOrCreateUser(userRepository, USER_SOFIA, "sofia@recipes.local",
                    "Collects cozy breakfast recipes and seasonal desserts"));
            users.put(USER_MAX, findOrCreateUser(userRepository, USER_MAX, "max@recipes.local",
                    "Tests quick meals for busy student life"));
            users.put(USER_DEMO_STUDENT, findOrCreateUser(userRepository, USER_DEMO_STUDENT,
                    "demo_student@recipes.local",
                    "Disposable lab profile. Safe to edit or delete before submission."));

            Map<String, Category> categories = new LinkedHashMap<>();
            categories.put(CATEGORY_SOUPS, findOrCreateCategory(categoryRepository, "Soups",
                    "Hot soups for everyday cooking"));
            categories.put(CATEGORY_DESSERTS, findOrCreateCategory(categoryRepository, "Desserts",
                    "Sweet recipes for tea and coffee"));
            categories.put(CATEGORY_BREAKFASTS, findOrCreateCategory(categoryRepository, "Breakfasts",
                    "Egg, toast and morning comfort food"));
            categories.put(CATEGORY_SALADS, findOrCreateCategory(categoryRepository, "Salads",
                    "Fresh and warm salads for lunch and dinner"));
            categories.put(CATEGORY_DRAFTS, findOrCreateCategory(categoryRepository, "Lab Drafts",
                    "Temporary recipes for CRUD demos. Safe to delete before submission."));
            categories.put(CATEGORY_GRILL, findOrCreateCategory(categoryRepository, "Grill",
                    "Recipes for grilling vegetables, meat and seafood"));
            categories.put(CATEGORY_VEGAN, findOrCreateCategory(categoryRepository, "Vegan",
                    "Plant-based recipes — currently empty, safe to delete"));

            Map<String, Ingredient> ingredients = new LinkedHashMap<>();
            ingredients.put(INGREDIENT_BEET, createIngredient("Beet", ingredientRepository));
            ingredients.put(INGREDIENT_POTATO, createIngredient("Potato", ingredientRepository));
            ingredients.put(INGREDIENT_SOUR_CREAM, createIngredient("Sour cream", ingredientRepository));
            ingredients.put(INGREDIENT_MASCARPONE, createIngredient("Mascarpone", ingredientRepository));
            ingredients.put(INGREDIENT_COFFEE, createIngredient("Coffee", ingredientRepository));
            ingredients.put(INGREDIENT_EGG, createIngredient("Egg", ingredientRepository));
            ingredients.put(INGREDIENT_TOMATO, createIngredient("Tomato", ingredientRepository));
            ingredients.put(INGREDIENT_BREAD, createIngredient("Bread", ingredientRepository));
            ingredients.put(INGREDIENT_FETA, createIngredient("Feta", ingredientRepository));
            ingredients.put(INGREDIENT_CUCUMBER, createIngredient("Cucumber", ingredientRepository));
            ingredients.put(INGREDIENT_CHICKEN, createIngredient("Chicken fillet", ingredientRepository));
            ingredients.put(INGREDIENT_LETTUCE, createIngredient("Lettuce", ingredientRepository));
            ingredients.put(INGREDIENT_PARMESAN, createIngredient("Parmesan", ingredientRepository));
            ingredients.put(INGREDIENT_PUMPKIN, createIngredient("Pumpkin", ingredientRepository));
            ingredients.put(INGREDIENT_CREAM, createIngredient("Cream", ingredientRepository));
            ingredients.put(INGREDIENT_GARLIC, createIngredient("Garlic", ingredientRepository));
            ingredients.put(INGREDIENT_PASTA, createIngredient("Pasta", ingredientRepository));
            ingredients.put(INGREDIENT_OLIVE_OIL, createIngredient("Olive oil", ingredientRepository));
            ingredients.put(INGREDIENT_BANANA, createIngredient("Banana", ingredientRepository));
            ingredients.put(INGREDIENT_MILK, createIngredient("Milk", ingredientRepository));
            ingredients.put(INGREDIENT_HONEY, createIngredient("Honey", ingredientRepository));
            ingredients.put(INGREDIENT_FLOUR, createIngredient("Flour", ingredientRepository));
            ingredients.put(INGREDIENT_SUGAR, createIngredient("Sugar", ingredientRepository));
            ingredients.put(INGREDIENT_BUTTER, createIngredient("Butter", ingredientRepository));
            ingredients.put(INGREDIENT_SHRIMP, createIngredient("Shrimp", ingredientRepository));
            ingredients.put(INGREDIENT_AVOCADO, createIngredient("Avocado", ingredientRepository));
            ingredients.put(INGREDIENT_DARK_CHOCOLATE, createIngredient("Dark chocolate", ingredientRepository));
            ingredients.put(INGREDIENT_NOODLES, createIngredient("Egg noodles", ingredientRepository));
            ingredients.put(INGREDIENT_CARROT, createIngredient("Carrot", ingredientRepository));
            ingredients.put(INGREDIENT_ONION, createIngredient("Onion", ingredientRepository));
            ingredients.put(INGREDIENT_BERRIES, createIngredient("Mixed berries", ingredientRepository));
            ingredients.put(INGREDIENT_YOGURT, createIngredient("Greek yogurt", ingredientRepository));
            ingredients.put(INGREDIENT_TUNA, createIngredient("Canned tuna", ingredientRepository));
            ingredients.put(INGREDIENT_ZUCCHINI, createIngredient("Zucchini", ingredientRepository));
            ingredients.put(INGREDIENT_PEPPER, createIngredient("Bell pepper", ingredientRepository));
            ingredients.put(INGREDIENT_LEMON, createIngredient("Lemon", ingredientRepository));

            createRecipeIfMissing(
                    recipeRepository,
                    "Borscht",
                    "Classic beet soup with vegetables and sour cream",
                    users.get(USER_ANNA),
                    categories.get(CATEGORY_SOUPS),
                    List.of(ingredients.get(INGREDIENT_BEET), ingredients.get(INGREDIENT_POTATO), ingredients.get(INGREDIENT_SOUR_CREAM)),
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
                    users.get(USER_NIKITA),
                    categories.get(CATEGORY_DESSERTS),
                    List.of(ingredients.get(INGREDIENT_MASCARPONE), ingredients.get(INGREDIENT_COFFEE), ingredients.get(INGREDIENT_EGG)),
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
                    users.get(USER_SOFIA),
                    categories.get(CATEGORY_BREAKFASTS),
                    List.of(ingredients.get(INGREDIENT_EGG), ingredients.get(INGREDIENT_TOMATO), ingredients.get(INGREDIENT_GARLIC), ingredients.get(INGREDIENT_OLIVE_OIL)),
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
                    users.get(USER_ANNA),
                    categories.get(CATEGORY_SALADS),
                    List.of(ingredients.get(INGREDIENT_TOMATO), ingredients.get(INGREDIENT_CUCUMBER), ingredients.get(INGREDIENT_FETA), ingredients.get(INGREDIENT_OLIVE_OIL)),
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
                    users.get(USER_MAX),
                    categories.get(CATEGORY_SALADS),
                    List.of(ingredients.get(INGREDIENT_CHICKEN), ingredients.get(INGREDIENT_LETTUCE), ingredients.get(INGREDIENT_PARMESAN), ingredients.get(INGREDIENT_BREAD)),
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
                    users.get(USER_NIKITA),
                    categories.get(CATEGORY_SOUPS),
                    List.of(ingredients.get(INGREDIENT_PUMPKIN), ingredients.get(INGREDIENT_CREAM), ingredients.get(INGREDIENT_GARLIC), ingredients.get(INGREDIENT_OLIVE_OIL)),
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
                    users.get(USER_DEMO_STUDENT),
                    categories.get(CATEGORY_DRAFTS),
                    List.of(ingredients.get(INGREDIENT_PASTA), ingredients.get(INGREDIENT_GARLIC), ingredients.get(INGREDIENT_OLIVE_OIL), ingredients.get(INGREDIENT_PARMESAN)),
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
                    users.get(USER_DEMO_STUDENT),
                    categories.get(CATEGORY_DRAFTS),
                    List.of(ingredients.get(INGREDIENT_BANANA), ingredients.get(INGREDIENT_MILK), ingredients.get(INGREDIENT_HONEY)),
                    List.of(
                            "Slice the banana",
                            "Blend banana with milk",
                            "Add honey and blend again"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Classic Pancakes",
                    "Fluffy pancakes for a perfect Sunday breakfast",
                    users.get(USER_SOFIA),
                    categories.get(CATEGORY_BREAKFASTS),
                    List.of(ingredients.get(INGREDIENT_FLOUR), ingredients.get(INGREDIENT_MILK), ingredients.get(INGREDIENT_EGG), ingredients.get(INGREDIENT_SUGAR), ingredients.get(INGREDIENT_BUTTER)),
                    List.of(
                            "Mix dry ingredients in a bowl",
                            "Whisk eggs with milk and melted butter",
                            "Combine wet and dry ingredients",
                            "Fry on a non-stick pan until golden"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Shrimp Caesar Salad",
                    "Classic Caesar with grilled shrimps instead of chicken",
                    users.get(USER_MAX),
                    categories.get(CATEGORY_SALADS),
                    List.of(ingredients.get(INGREDIENT_SHRIMP), ingredients.get(INGREDIENT_LETTUCE), ingredients.get(INGREDIENT_PARMESAN), ingredients.get(INGREDIENT_BREAD), ingredients.get(INGREDIENT_GARLIC)),
                    List.of(
                            "Marinate shrimps with garlic and olive oil",
                            "Grill shrimps for 2 minutes per side",
                            "Toast croutons",
                            "Toss lettuce with dressing, top with shrimps and parmesan"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Chocolate Mousse",
                    "Light and airy dark chocolate mousse",
                    users.get(USER_ANNA),
                    categories.get(CATEGORY_DESSERTS),
                    List.of(ingredients.get(INGREDIENT_DARK_CHOCOLATE), ingredients.get(INGREDIENT_EGG), ingredients.get(INGREDIENT_SUGAR)),
                    List.of(
                            "Melt chocolate in a water bath",
                            "Separate egg yolks from whites",
                            "Whisk yolks with sugar, add to chocolate",
                            "Beat egg whites until stiff, fold into chocolate mixture",
                            "Chill for 2 hours before serving"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Chicken Noodle Soup",
                    "Hearty soup with homemade egg noodles",
                    users.get(USER_ANNA),
                    categories.get(CATEGORY_SOUPS),
                    List.of(ingredients.get(INGREDIENT_CHICKEN), ingredients.get(INGREDIENT_NOODLES), ingredients.get(INGREDIENT_CARROT), ingredients.get(INGREDIENT_ONION), ingredients.get(INGREDIENT_GARLIC)),
                    List.of(
                            "Simmer chicken with vegetables to make broth",
                            "Remove chicken, shred meat",
                            "Add noodles and cook until tender",
                            "Return shredded chicken, season with salt and pepper"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Berry Yogurt Bowl",
                    "Quick healthy breakfast with Greek yogurt and berries",
                    users.get(USER_SOFIA),
                    categories.get(CATEGORY_BREAKFASTS),
                    List.of(ingredients.get(INGREDIENT_YOGURT), ingredients.get(INGREDIENT_BERRIES), ingredients.get(INGREDIENT_HONEY)),
                    List.of(
                            "Spoon yogurt into a bowl",
                            "Top with mixed berries",
                            "Drizzle with honey and serve"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Tuna Avocado Salad",
                    "No-mayo tuna salad with avocado and cucumber",
                    users.get(USER_NIKITA),
                    categories.get(CATEGORY_SALADS),
                    List.of(ingredients.get(INGREDIENT_TUNA), ingredients.get(INGREDIENT_AVOCADO), ingredients.get(INGREDIENT_CUCUMBER), ingredients.get(INGREDIENT_OLIVE_OIL), ingredients.get(INGREDIENT_LEMON)),
                    List.of(
                            "Drain tuna and flake with a fork",
                            "Dice avocado and cucumber",
                            "Mix all ingredients with olive oil and lemon juice",
                            "Serve on lettuce leaves"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Grilled Vegetables",
                    "Colorful zucchini and bell peppers on the grill",
                    users.get(USER_MAX),
                    categories.get(CATEGORY_GRILL),
                    List.of(ingredients.get(INGREDIENT_ZUCCHINI), ingredients.get(INGREDIENT_PEPPER), ingredients.get(INGREDIENT_OLIVE_OIL), ingredients.get(INGREDIENT_GARLIC)),
                    List.of(
                            "Slice zucchini and bell peppers into thick pieces",
                            "Brush with olive oil and minced garlic",
                            "Grill on medium heat for 3-4 minutes per side",
                            "Sprinkle with salt and serve warm"
                    )
            );
        });
    }

    private Ingredient createIngredient(String name, IngredientRepository ingredientRepository) {
        return ingredientRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setName(name);
                    return ingredientRepository.save(ingredient);
                });
    }

    private User findOrCreateUser(UserRepository userRepository, String username, String email, String bio) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setBio(bio);
                    return userRepository.save(user);
                });
    }

    private Category findOrCreateCategory(CategoryRepository categoryRepository, String name, String description) {
        return categoryRepository.findByNameIgnoreCase(name)
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