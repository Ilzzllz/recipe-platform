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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
    private static final String INGREDIENT_LEMON = "lemon";

    @Bean
    @ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
    CommandLineRunner seedData(UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               IngredientRepository ingredientRepository,
                               RecipeRepository recipeRepository,
                               TransactionTemplate transactionTemplate) {
        return args -> transactionTemplate.executeWithoutResult(status -> {
            localizeExistingSeedData(userRepository, categoryRepository, ingredientRepository, recipeRepository);
            Map<String, User> users = new LinkedHashMap<>();
            users.put(USER_ANNA, findOrCreateUser(userRepository, USER_ANNA, "anna@recipes.local",
                    "Домашний повар, который делится семейными рецептами"));
            users.put(USER_NIKITA, findOrCreateUser(userRepository, USER_NIKITA, "nikita@recipes.local",
                    "Любит быстрые блюда для будних вечеров"));
            users.put(USER_SOFIA, findOrCreateUser(userRepository, USER_SOFIA, "sofia@recipes.local",
                    "Собирает уютные завтраки и сезонные десерты"));
            users.put(USER_MAX, findOrCreateUser(userRepository, USER_MAX, "max@recipes.local",
                    "Проверяет быстрые блюда для активной студенческой жизни"));
            users.put(USER_DEMO_STUDENT, findOrCreateUser(userRepository, USER_DEMO_STUDENT,
                    "demo_student@recipes.local",
                    "Временный лабораторный профиль. Его можно изменить или удалить."));

            Map<String, Category> categories = new LinkedHashMap<>();
            categories.put(CATEGORY_SOUPS, findOrCreateCategory(categoryRepository, "Супы",
                    "Горячие супы для повседневной кухни"));
            categories.put(CATEGORY_DESSERTS, findOrCreateCategory(categoryRepository, "Десерты",
                    "Сладкие блюда к чаю и кофе"));
            categories.put(CATEGORY_BREAKFASTS, findOrCreateCategory(categoryRepository, "Завтраки",
                    "Яичные блюда, тосты и сытная утренняя еда"));
            categories.put(CATEGORY_SALADS, findOrCreateCategory(categoryRepository, "Салаты",
                    "Свежие и тёплые салаты для обеда и ужина"));
            categories.put(CATEGORY_DRAFTS, findOrCreateCategory(categoryRepository, "Черновики лаборатории",
                    "Временные рецепты для демонстрации операций создания, чтения, обновления и удаления. Их можно удалить."));
            categories.put(CATEGORY_GRILL, findOrCreateCategory(categoryRepository, "Гриль",
                    "Овощи, мясо и морепродукты на гриле"));
            categories.put(CATEGORY_VEGAN, findOrCreateCategory(categoryRepository, "Веганские блюда",
                    "Растительные рецепты"));

            Map<String, Ingredient> ingredients = new LinkedHashMap<>();
            ingredients.put(INGREDIENT_BEET, createIngredient("Свёкла", ingredientRepository));
            ingredients.put(INGREDIENT_POTATO, createIngredient("Картофель", ingredientRepository));
            ingredients.put(INGREDIENT_SOUR_CREAM, createIngredient("Сметана", ingredientRepository));
            ingredients.put(INGREDIENT_MASCARPONE, createIngredient("Маскарпоне", ingredientRepository));
            ingredients.put(INGREDIENT_COFFEE, createIngredient("Кофе", ingredientRepository));
            ingredients.put(INGREDIENT_EGG, createIngredient("Яйцо", ingredientRepository));
            ingredients.put(INGREDIENT_TOMATO, createIngredient("Помидор", ingredientRepository));
            ingredients.put(INGREDIENT_BREAD, createIngredient("Хлеб", ingredientRepository));
            ingredients.put(INGREDIENT_FETA, createIngredient("Фета", ingredientRepository));
            ingredients.put(INGREDIENT_CUCUMBER, createIngredient("Огурец", ingredientRepository));
            ingredients.put(INGREDIENT_CHICKEN, createIngredient("Куриное филе", ingredientRepository));
            ingredients.put(INGREDIENT_LETTUCE, createIngredient("Листья салата", ingredientRepository));
            ingredients.put(INGREDIENT_PARMESAN, createIngredient("Пармезан", ingredientRepository));
            ingredients.put(INGREDIENT_PUMPKIN, createIngredient("Тыква", ingredientRepository));
            ingredients.put(INGREDIENT_CREAM, createIngredient("Сливки", ingredientRepository));
            ingredients.put(INGREDIENT_GARLIC, createIngredient("Чеснок", ingredientRepository));
            ingredients.put(INGREDIENT_PASTA, createIngredient("Паста", ingredientRepository));
            ingredients.put(INGREDIENT_OLIVE_OIL, createIngredient("Оливковое масло", ingredientRepository));
            ingredients.put(INGREDIENT_BANANA, createIngredient("Банан", ingredientRepository));
            ingredients.put(INGREDIENT_MILK, createIngredient("Молоко", ingredientRepository));
            ingredients.put(INGREDIENT_HONEY, createIngredient("Мёд", ingredientRepository));
            ingredients.put(INGREDIENT_FLOUR, createIngredient("Мука", ingredientRepository));
            ingredients.put(INGREDIENT_SUGAR, createIngredient("Сахар", ingredientRepository));
            ingredients.put(INGREDIENT_BUTTER, createIngredient("Сливочное масло", ingredientRepository));
            ingredients.put(INGREDIENT_SHRIMP, createIngredient("Креветки", ingredientRepository));
            ingredients.put(INGREDIENT_AVOCADO, createIngredient("Авокадо", ingredientRepository));
            ingredients.put(INGREDIENT_DARK_CHOCOLATE, createIngredient("Тёмный шоколад", ingredientRepository));
            ingredients.put(INGREDIENT_NOODLES, createIngredient("Яичная лапша", ingredientRepository));
            ingredients.put(INGREDIENT_CARROT, createIngredient("Морковь", ingredientRepository));
            ingredients.put(INGREDIENT_ONION, createIngredient("Лук", ingredientRepository));
            ingredients.put(INGREDIENT_BERRIES, createIngredient("Лесные ягоды", ingredientRepository));
            ingredients.put(INGREDIENT_YOGURT, createIngredient("Греческий йогурт", ingredientRepository));
            ingredients.put(INGREDIENT_TUNA, createIngredient("Консервированный тунец", ingredientRepository));
            ingredients.put(INGREDIENT_ZUCCHINI, createIngredient("Кабачок", ingredientRepository));
            ingredients.put(INGREDIENT_PEPPER, createIngredient("Болгарский перец", ingredientRepository));
            ingredients.put(INGREDIENT_LEMON, createIngredient("Лимон", ingredientRepository));

            createRecipeIfMissing(
                    recipeRepository,
                    "Борщ",
                    "Классический свекольный суп с овощами и сметаной",
                    users.get(USER_ANNA),
                    categories.get(CATEGORY_SOUPS),
                    List.of(ingredients.get(INGREDIENT_BEET), ingredients.get(INGREDIENT_POTATO), ingredients.get(INGREDIENT_SOUR_CREAM)),
                    List.of(
                            "Подготовить овощи и основу для бульона",
                            "Варить до мягкости овощей",
                            "Подать с ложкой сметаны"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Тирамису",
                    "Слоёный десерт с кофе и маскарпоне",
                    users.get(USER_NIKITA),
                    categories.get(CATEGORY_DESSERTS),
                    List.of(ingredients.get(INGREDIENT_MASCARPONE), ingredients.get(INGREDIENT_COFFEE), ingredients.get(INGREDIENT_EGG)),
                    List.of(
                            "Взбить крем из маскарпоне",
                            "Пропитать печенье кофе",
                            "Выложить слоями и охладить перед подачей"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Шакшука",
                    "Яйца, запечённые в остром томатном соусе",
                    users.get(USER_SOFIA),
                    categories.get(CATEGORY_BREAKFASTS),
                    List.of(ingredients.get(INGREDIENT_EGG), ingredients.get(INGREDIENT_TOMATO), ingredients.get(INGREDIENT_GARLIC), ingredients.get(INGREDIENT_OLIVE_OIL)),
                    List.of(
                            "Обжарить чеснок в оливковом масле",
                            "Тушить томатную основу до загустения",
                            "Разбить яйца в соус и запекать до готовности"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Греческий салат",
                    "Свежий салат с огурцом, помидором и фетой",
                    users.get(USER_ANNA),
                    categories.get(CATEGORY_SALADS),
                    List.of(ingredients.get(INGREDIENT_TOMATO), ingredients.get(INGREDIENT_CUCUMBER), ingredients.get(INGREDIENT_FETA), ingredients.get(INGREDIENT_OLIVE_OIL)),
                    List.of(
                            "Нарезать овощи небольшими кусочками",
                            "Добавить фету и полить оливковым маслом",
                            "Осторожно перемешать и сразу подать"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Салат Цезарь с курицей",
                    "Тёплый салат с курицей, листьями салата и пармезаном",
                    users.get(USER_MAX),
                    categories.get(CATEGORY_SALADS),
                    List.of(ingredients.get(INGREDIENT_CHICKEN), ingredients.get(INGREDIENT_LETTUCE), ingredients.get(INGREDIENT_PARMESAN), ingredients.get(INGREDIENT_BREAD)),
                    List.of(
                            "Обжарить курицу до золотистой корочки",
                            "Подсушить хлеб до хрустящих сухариков",
                            "Соединить салат, курицу, пармезан и сухарики"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Тыквенный крем-суп",
                    "Нежный осенний суп-пюре из тыквы со сливками",
                    users.get(USER_NIKITA),
                    categories.get(CATEGORY_SOUPS),
                    List.of(ingredients.get(INGREDIENT_PUMPKIN), ingredients.get(INGREDIENT_CREAM), ingredients.get(INGREDIENT_GARLIC), ingredients.get(INGREDIENT_OLIVE_OIL)),
                    List.of(
                            "Запечь тыкву с чесноком",
                            "Взбить с тёплыми сливками до однородности",
                            "Аккуратно прогреть и приправить перед подачей"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Лабораторная паста",
                    "Временный рецепт для демонстрации операций создания, чтения, обновления и удаления, а также транзакций. Его можно удалить.",
                    users.get(USER_DEMO_STUDENT),
                    categories.get(CATEGORY_DRAFTS),
                    List.of(ingredients.get(INGREDIENT_PASTA), ingredients.get(INGREDIENT_GARLIC), ingredients.get(INGREDIENT_OLIVE_OIL), ingredients.get(INGREDIENT_PARMESAN)),
                    List.of(
                            "Отварить пасту до состояния аль денте",
                            "Прогреть чеснок в оливковом масле, не пережаривая",
                            "Смешать пасту с маслом и добавить пармезан"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Лабораторный смузи",
                    "Временный пример завтрака для проверки обновления и удаления.",
                    users.get(USER_DEMO_STUDENT),
                    categories.get(CATEGORY_DRAFTS),
                    List.of(ingredients.get(INGREDIENT_BANANA), ingredients.get(INGREDIENT_MILK), ingredients.get(INGREDIENT_HONEY)),
                    List.of(
                            "Нарезать банан",
                            "Взбить банан с молоком",
                            "Добавить мёд и ещё раз взбить"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Классические блины",
                    "Пышные блины для идеального воскресного завтрака",
                    users.get(USER_SOFIA),
                    categories.get(CATEGORY_BREAKFASTS),
                    List.of(ingredients.get(INGREDIENT_FLOUR), ingredients.get(INGREDIENT_MILK), ingredients.get(INGREDIENT_EGG), ingredients.get(INGREDIENT_SUGAR), ingredients.get(INGREDIENT_BUTTER)),
                    List.of(
                            "Смешать сухие ингредиенты в миске",
                            "Взбить яйца с молоком и растопленным маслом",
                            "Соединить сухие и жидкие ингредиенты",
                            "Жарить на антипригарной сковороде до золотистого цвета"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Салат Цезарь с креветками",
                    "Классический Цезарь с креветками вместо курицы",
                    users.get(USER_MAX),
                    categories.get(CATEGORY_SALADS),
                    List.of(ingredients.get(INGREDIENT_SHRIMP), ingredients.get(INGREDIENT_LETTUCE), ingredients.get(INGREDIENT_PARMESAN), ingredients.get(INGREDIENT_BREAD), ingredients.get(INGREDIENT_GARLIC)),
                    List.of(
                            "Замариновать креветки с чесноком и оливковым маслом",
                            "Обжарить креветки на гриле по 2 минуты с каждой стороны",
                            "Подсушить сухарики",
                            "Перемешать салат с заправкой, сверху выложить креветки и пармезан"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Шоколадный мусс",
                    "Лёгкий воздушный мусс из тёмного шоколада",
                    users.get(USER_ANNA),
                    categories.get(CATEGORY_DESSERTS),
                    List.of(ingredients.get(INGREDIENT_DARK_CHOCOLATE), ingredients.get(INGREDIENT_EGG), ingredients.get(INGREDIENT_SUGAR)),
                    List.of(
                            "Растопить шоколад на водяной бане",
                            "Отделить желтки от белков",
                            "Взбить желтки с сахаром и добавить к шоколаду",
                            "Взбить белки до плотной пены и аккуратно вмешать в шоколад",
                            "Охладить 2 часа перед подачей"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Куриный суп с лапшой",
                    "Сытный суп с домашней яичной лапшой",
                    users.get(USER_ANNA),
                    categories.get(CATEGORY_SOUPS),
                    List.of(ingredients.get(INGREDIENT_CHICKEN), ingredients.get(INGREDIENT_NOODLES), ingredients.get(INGREDIENT_CARROT), ingredients.get(INGREDIENT_ONION), ingredients.get(INGREDIENT_GARLIC)),
                    List.of(
                            "Сварить курицу с овощами для бульона",
                            "Достать курицу и разобрать мясо",
                            "Добавить лапшу и варить до мягкости",
                            "Вернуть мясо в суп, посолить и поперчить"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Йогуртовая тарелка с ягодами",
                    "Быстрый полезный завтрак с греческим йогуртом и ягодами",
                    users.get(USER_SOFIA),
                    categories.get(CATEGORY_BREAKFASTS),
                    List.of(ingredients.get(INGREDIENT_YOGURT), ingredients.get(INGREDIENT_BERRIES), ingredients.get(INGREDIENT_HONEY)),
                    List.of(
                            "Выложить йогурт в миску",
                            "Сверху добавить ягоды",
                            "Полить мёдом и подать"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Салат с тунцом и авокадо",
                    "Салат с тунцом, авокадо и огурцом без майонеза",
                    users.get(USER_NIKITA),
                    categories.get(CATEGORY_SALADS),
                    List.of(ingredients.get(INGREDIENT_TUNA), ingredients.get(INGREDIENT_AVOCADO), ingredients.get(INGREDIENT_CUCUMBER), ingredients.get(INGREDIENT_OLIVE_OIL), ingredients.get(INGREDIENT_LEMON)),
                    List.of(
                            "Слить жидкость с тунца и размять его вилкой",
                            "Нарезать авокадо и огурец кубиками",
                            "Смешать ингредиенты с оливковым маслом и лимонным соком",
                            "Подать на листьях салата"
                    )
            );

            createRecipeIfMissing(
                    recipeRepository,
                    "Овощи на гриле",
                    "Яркие кабачки и болгарский перец на гриле",
                    users.get(USER_MAX),
                    categories.get(CATEGORY_GRILL),
                    List.of(ingredients.get(INGREDIENT_ZUCCHINI), ingredients.get(INGREDIENT_PEPPER), ingredients.get(INGREDIENT_OLIVE_OIL), ingredients.get(INGREDIENT_GARLIC)),
                    List.of(
                            "Нарезать кабачок и перец толстыми кусочками",
                            "Смазать оливковым маслом и измельчённым чесноком",
                            "Готовить на среднем огне по 3–4 минуты с каждой стороны",
                            "Посыпать солью и подать тёплыми"
                    )
            );
        });
    }

    /**
     * Converts the original demo seed (which was written in English) in-place.
     * This keeps an existing developer database consistent after upgrading the UI;
     * no ids or recipe relationships are changed.
     */
    private void localizeExistingSeedData(UserRepository userRepository,
                                          CategoryRepository categoryRepository,
                                          IngredientRepository ingredientRepository,
                                          RecipeRepository recipeRepository) {
        Map<String, String> userBios = Map.ofEntries(
                Map.entry("anna", "Домашний повар, который делится семейными рецептами"),
                Map.entry("nikita", "Любит быстрые блюда для будних вечеров"),
                Map.entry("sofia", "Собирает уютные завтраки и сезонные десерты"),
                Map.entry("max", "Проверяет быстрые блюда для активной студенческой жизни"),
                Map.entry("demo_student", "Временный лабораторный профиль. Его можно изменить или удалить.")
        );
        userRepository.findAll().forEach(user -> {
            String localizedBio = userBios.get(user.getUsername());
            if (localizedBio != null) user.setBio(localizedBio);
        });
        userRepository.flush();

        Map<String, String> categoryNames = Map.ofEntries(
                Map.entry("Soups", "Супы"),
                Map.entry("Desserts", "Десерты"),
                Map.entry("Breakfasts", "Завтраки"),
                Map.entry("Salads", "Салаты"),
                Map.entry("Lab Drafts", "Черновики лаборатории"),
                Map.entry("Grill", "Гриль"),
                Map.entry("Vegan", "Веганские блюда")
        );
        Map<String, String> categoryDescriptions = Map.ofEntries(
                Map.entry("Супы", "Горячие супы для повседневной кухни"),
                Map.entry("Десерты", "Сладкие блюда к чаю и кофе"),
                Map.entry("Завтраки", "Яичные блюда, тосты и сытная утренняя еда"),
                Map.entry("Салаты", "Свежие и тёплые салаты для обеда и ужина"),
                Map.entry("Черновики лаборатории", "Временные рецепты для демонстрации операций создания, чтения, обновления и удаления. Их можно удалить."),
                Map.entry("Гриль", "Овощи, мясо и морепродукты на гриле"),
                Map.entry("Веганские блюда", "Растительные рецепты")
        );
        categoryRepository.findAll().forEach(category -> {
            String localizedName = categoryNames.getOrDefault(category.getName(), category.getName());
            category.setName(localizedName);
            if (categoryDescriptions.containsKey(localizedName)) {
                category.setDescription(categoryDescriptions.get(localizedName));
            }
        });
        categoryRepository.flush();

        Map<String, String> ingredientNames = Map.ofEntries(
                Map.entry("Beet", "Свёкла"), Map.entry("Potato", "Картофель"),
                Map.entry("Sour cream", "Сметана"), Map.entry("Mascarpone", "Маскарпоне"),
                Map.entry("Coffee", "Кофе"), Map.entry("Egg", "Яйцо"),
                Map.entry("Tomato", "Помидор"), Map.entry("Bread", "Хлеб"),
                Map.entry("Feta", "Фета"), Map.entry("Cucumber", "Огурец"),
                Map.entry("Chicken fillet", "Куриное филе"), Map.entry("Lettuce", "Листья салата"),
                Map.entry("Parmesan", "Пармезан"), Map.entry("Pumpkin", "Тыква"),
                Map.entry("Cream", "Сливки"), Map.entry("Garlic", "Чеснок"),
                Map.entry("Pasta", "Паста"), Map.entry("Olive oil", "Оливковое масло"),
                Map.entry("Banana", "Банан"), Map.entry("Milk", "Молоко"),
                Map.entry("Honey", "Мёд"), Map.entry("Flour", "Мука"),
                Map.entry("Sugar", "Сахар"), Map.entry("Butter", "Сливочное масло"),
                Map.entry("Shrimp", "Креветки"), Map.entry("Avocado", "Авокадо"),
                Map.entry("Dark chocolate", "Тёмный шоколад"), Map.entry("Egg noodles", "Яичная лапша"),
                Map.entry("Carrot", "Морковь"), Map.entry("Onion", "Лук"),
                Map.entry("Mixed berries", "Лесные ягоды"), Map.entry("Greek yogurt", "Греческий йогурт"),
                Map.entry("Canned tuna", "Консервированный тунец"), Map.entry("Zucchini", "Кабачок"),
                Map.entry("Bell pepper", "Болгарский перец"), Map.entry("Lemon", "Лимон")
        );
        ingredientRepository.findAll().forEach(ingredient ->
                ingredient.setName(ingredientNames.getOrDefault(ingredient.getName(), ingredient.getName())));
        ingredientRepository.flush();

        Map<String, String> recipeTitles = Map.ofEntries(
                Map.entry("Borscht", "Борщ"), Map.entry("Tiramisu", "Тирамису"),
                Map.entry("Shakshuka", "Шакшука"), Map.entry("Greek Salad", "Греческий салат"),
                Map.entry("Caesar Chicken Salad", "Салат Цезарь с курицей"), Map.entry("Pumpkin Cream Soup", "Тыквенный крем-суп"),
                Map.entry("demo_lab_pasta", "Лабораторная паста"), Map.entry("demo_lab_smoothie", "Лабораторный смузи"),
                Map.entry("Classic Pancakes", "Классические блины"), Map.entry("Shrimp Caesar Salad", "Салат Цезарь с креветками"),
                Map.entry("Chocolate Mousse", "Шоколадный мусс"), Map.entry("Chicken Noodle Soup", "Куриный суп с лапшой"),
                Map.entry("Berry Yogurt Bowl", "Йогуртовая тарелка с ягодами"), Map.entry("Tuna Avocado Salad", "Салат с тунцом и авокадо"),
                Map.entry("Grilled Vegetables", "Овощи на гриле")
        );
        Map<String, String> recipeDescriptions = Map.ofEntries(
                Map.entry("Borscht", "Классический свекольный суп с овощами и сметаной"),
                Map.entry("Tiramisu", "Слоёный десерт с кофе и маскарпоне"),
                Map.entry("Shakshuka", "Яйца, запечённые в остром томатном соусе"),
                Map.entry("Greek Salad", "Свежий салат с огурцом, помидором и фетой"),
                Map.entry("Caesar Chicken Salad", "Тёплый салат с курицей, листьями салата и пармезаном"),
                Map.entry("Pumpkin Cream Soup", "Нежный осенний суп-пюре из тыквы со сливками"),
                Map.entry("demo_lab_pasta", "Временный рецепт для демонстрации операций создания, чтения, обновления и удаления, а также транзакций. Его можно удалить."),
                Map.entry("demo_lab_smoothie", "Временный пример завтрака для проверки обновления и удаления."),
                Map.entry("Classic Pancakes", "Пышные блины для идеального воскресного завтрака"),
                Map.entry("Shrimp Caesar Salad", "Классический Цезарь с креветками вместо курицы"),
                Map.entry("Chocolate Mousse", "Лёгкий воздушный мусс из тёмного шоколада"),
                Map.entry("Chicken Noodle Soup", "Сытный суп с домашней яичной лапшой"),
                Map.entry("Berry Yogurt Bowl", "Быстрый полезный завтрак с греческим йогуртом и ягодами"),
                Map.entry("Tuna Avocado Salad", "Салат с тунцом, авокадо и огурцом без майонеза"),
                Map.entry("Grilled Vegetables", "Яркие кабачки и болгарский перец на гриле")
        );
        Map<String, String> stepTranslations = Map.ofEntries(
                Map.entry("Prepare the vegetables and broth base", "Подготовить овощи и основу для бульона"),
                Map.entry("Simmer until the vegetables are tender", "Варить до мягкости овощей"),
                Map.entry("Serve with a spoon of sour cream", "Подать с ложкой сметаны"),
                Map.entry("Whisk the mascarpone cream", "Взбить крем из маскарпоне"),
                Map.entry("Soak biscuits in coffee", "Пропитать печенье кофе"),
                Map.entry("Layer and chill before serving", "Выложить слоями и охладить перед подачей"),
                Map.entry("Saute garlic in olive oil", "Обжарить чеснок в оливковом масле"),
                Map.entry("Cook the tomato base until thickened", "Тушить томатную основу до загустения"),
                Map.entry("Crack eggs into the sauce and bake until set", "Разбить яйца в соус и запекать до готовности"),
                Map.entry("Chop the vegetables into bite-size pieces", "Нарезать овощи небольшими кусочками"),
                Map.entry("Add feta and drizzle with olive oil", "Добавить фету и полить оливковым маслом"),
                Map.entry("Mix gently and serve immediately", "Осторожно перемешать и сразу подать"),
                Map.entry("Pan-fry the chicken until golden", "Обжарить курицу до золотистой корочки"),
                Map.entry("Toast the bread into crunchy croutons", "Подсушить хлеб до хрустящих сухариков"),
                Map.entry("Combine lettuce, chicken, parmesan and croutons", "Соединить салат, курицу, пармезан и сухарики"),
                Map.entry("Roast the pumpkin with garlic", "Запечь тыкву с чесноком"),
                Map.entry("Blend with warm cream until smooth", "Взбить с тёплыми сливками до однородности"),
                Map.entry("Heat gently and season before serving", "Аккуратно прогреть и приправить перед подачей"),
                Map.entry("Boil the pasta until al dente", "Отварить пасту до состояния аль денте"),
                Map.entry("Warm garlic in olive oil without burning it", "Прогреть чеснок в оливковом масле, не пережаривая"),
                Map.entry("Mix pasta with oil and finish with parmesan", "Смешать пасту с маслом и добавить пармезан"),
                Map.entry("Slice the banana", "Нарезать банан"),
                Map.entry("Blend banana with milk", "Взбить банан с молоком"),
                Map.entry("Add honey and blend again", "Добавить мёд и ещё раз взбить"),
                Map.entry("Mix dry ingredients in a bowl", "Смешать сухие ингредиенты в миске"),
                Map.entry("Whisk eggs with milk and melted butter", "Взбить яйца с молоком и растопленным маслом"),
                Map.entry("Combine wet and dry ingredients", "Соединить сухие и жидкие ингредиенты"),
                Map.entry("Fry on a non-stick pan until golden", "Жарить на антипригарной сковороде до золотистого цвета"),
                Map.entry("Marinate shrimps with garlic and olive oil", "Замариновать креветки с чесноком и оливковым маслом"),
                Map.entry("Grill shrimps for 2 minutes per side", "Обжарить креветки на гриле по 2 минуты с каждой стороны"),
                Map.entry("Toast croutons", "Подсушить сухарики"),
                Map.entry("Toss lettuce with dressing, top with shrimps and parmesan", "Перемешать салат с заправкой, сверху выложить креветки и пармезан"),
                Map.entry("Melt chocolate in a water bath", "Растопить шоколад на водяной бане"),
                Map.entry("Separate egg yolks from whites", "Отделить желтки от белков"),
                Map.entry("Whisk yolks with sugar, add to chocolate", "Взбить желтки с сахаром и добавить к шоколаду"),
                Map.entry("Beat egg whites until stiff, fold into chocolate mixture", "Взбить белки до плотной пены и аккуратно вмешать в шоколад"),
                Map.entry("Chill for 2 hours before serving", "Охладить 2 часа перед подачей"),
                Map.entry("Simmer chicken with vegetables to make broth", "Сварить курицу с овощами для бульона"),
                Map.entry("Remove chicken, shred meat", "Достать курицу и разобрать мясо"),
                Map.entry("Add noodles and cook until tender", "Добавить лапшу и варить до мягкости"),
                Map.entry("Return shredded chicken, season with salt and pepper", "Вернуть мясо в суп, посолить и поперчить"),
                Map.entry("Spoon yogurt into a bowl", "Выложить йогурт в миску"),
                Map.entry("Top with mixed berries", "Сверху добавить ягоды"),
                Map.entry("Drizzle with honey and serve", "Полить мёдом и подать"),
                Map.entry("Drain tuna and flake with a fork", "Слить жидкость с тунца и размять его вилкой"),
                Map.entry("Dice avocado and cucumber", "Нарезать авокадо и огурец кубиками"),
                Map.entry("Mix all ingredients with olive oil and lemon juice", "Смешать ингредиенты с оливковым маслом и лимонным соком"),
                Map.entry("Serve on lettuce leaves", "Подать на листьях салата"),
                Map.entry("Slice zucchini and bell peppers into thick pieces", "Нарезать кабачок и перец толстыми кусочками"),
                Map.entry("Brush with olive oil and minced garlic", "Смазать оливковым маслом и измельчённым чесноком"),
                Map.entry("Grill on medium heat for 3-4 minutes per side", "Готовить на среднем огне по 3–4 минуты с каждой стороны"),
                Map.entry("Sprinkle with salt and serve warm", "Посыпать солью и подать тёплыми")
        );
        recipeRepository.findAllWithFetchJoin().forEach(recipe -> {
            String oldTitle = recipe.getTitle();
            if (!recipeTitles.containsKey(oldTitle)) {
                return;
            }
            recipe.setTitle(recipeTitles.get(oldTitle));
            recipe.setDescription(recipeDescriptions.get(oldTitle));
            recipe.getSteps().forEach(step -> {
                String localizedStep = stepTranslations.get(step.getDescription());
                if (localizedStep != null) {
                    step.setDescription(localizedStep);
                }
            });
        });
        recipeRepository.flush();
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
