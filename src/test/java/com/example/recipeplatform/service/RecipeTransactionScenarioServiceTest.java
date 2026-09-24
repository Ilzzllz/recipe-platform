package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.TransactionTestRequestDto;
import com.example.recipeplatform.exception.TransactionDemoException;
import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.User;
import com.example.recipeplatform.repository.CategoryRepository;
import com.example.recipeplatform.repository.IngredientRepository;
import com.example.recipeplatform.repository.RecipeRepository;
import com.example.recipeplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecipeTransactionScenarioServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private RecipeRepository recipeRepository;

    private RecipeTransactionScenarioService scenarioService;

    @BeforeEach
    void setUp() {
        scenarioService = new RecipeTransactionScenarioService(userRepository, categoryRepository,
                ingredientRepository, recipeRepository);
    }

    @Test
    @DisplayName("saveWithoutTransactional should save and flush user, category, ingredient and throw exception")
    void saveWithoutTransactionalShouldSaveEntitiesAndThrow() {
        TransactionTestRequestDto dto = sampleRequestDto("user", "email", "My bio", "cat", "Cat desc", "ing", "rec", "Rec desc");

        assertThatThrownBy(() -> scenarioService.saveWithoutTransactional(dto, "m1"))
                .isInstanceOf(TransactionDemoException.class)
                .hasMessageContaining("User, Category and Ingredient saved (partial commit)");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        assertThat(userCaptor.getValue().getUsername()).isEqualTo("m1_user");
        assertThat(userCaptor.getValue().getBio()).isEqualTo("My bio");

        ArgumentCaptor<Category> catCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).saveAndFlush(catCaptor.capture());
        assertThat(catCaptor.getValue().getName()).isEqualTo("m1_cat");
        assertThat(catCaptor.getValue().getDescription()).isEqualTo("Cat desc");

        ArgumentCaptor<Ingredient> ingCaptor = ArgumentCaptor.forClass(Ingredient.class);
        verify(ingredientRepository).saveAndFlush(ingCaptor.capture());
        assertThat(ingCaptor.getValue().getName()).isEqualTo("m1_ing");
    }

    @Test
    @DisplayName("saveWithoutTransactional should handle null optional fields")
    void saveWithoutTransactionalShouldHandleNullOptionalFields() {
        TransactionTestRequestDto dto = sampleRequestDto("user", "email", null, "cat", null, "ing", "rec", null);

        assertThatThrownBy(() -> scenarioService.saveWithoutTransactional(dto, "m2"))
                .isInstanceOf(TransactionDemoException.class);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        assertThat(userCaptor.getValue().getBio()).isEqualTo("Created for transaction demo (no tx)");

        ArgumentCaptor<Category> catCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).saveAndFlush(catCaptor.capture());
        assertThat(catCaptor.getValue().getDescription()).isEqualTo("Created for transaction demo (no tx)");
    }

    @Test
    @DisplayName("saveWithTransactional should save user, category, ingredient, recipe and throw exception")
    void saveWithTransactionalShouldSaveEntitiesAndThrow() {
        TransactionTestRequestDto dto = sampleRequestDto("user", "email", "My bio", "cat", "Cat desc", "ing", "rec", "Rec desc");

        assertThatThrownBy(() -> scenarioService.saveWithTransactional(dto, "m3"))
                .isInstanceOf(TransactionDemoException.class)
                .hasMessageContaining("Everything rolled back due to intentional failure.");

        verify(userRepository).save(any(User.class));
        verify(categoryRepository).save(any(Category.class));
        verify(ingredientRepository).save(any(Ingredient.class));

        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).save(recipeCaptor.capture());
        assertThat(recipeCaptor.getValue().getTitle()).isEqualTo("m3_rec");
        assertThat(recipeCaptor.getValue().getDescription()).isEqualTo("Rec desc");
    }

    @Test
    @DisplayName("saveWithTransactional should handle null optional fields")
    void saveWithTransactionalShouldHandleNullOptionalFields() {
        TransactionTestRequestDto dto = sampleRequestDto("user", "email", null, "cat", null, "ing", "rec", null);

        assertThatThrownBy(() -> scenarioService.saveWithTransactional(dto, "m4"))
                .isInstanceOf(TransactionDemoException.class);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getBio()).isEqualTo("Created for transaction demo (with tx)");

        ArgumentCaptor<Category> catCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(catCaptor.capture());
        assertThat(catCaptor.getValue().getDescription()).isEqualTo("Created for transaction demo (with tx)");

        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).save(recipeCaptor.capture());
        assertThat(recipeCaptor.getValue().getDescription()).isEqualTo("This recipe will be rolled back");
    }

    private TransactionTestRequestDto sampleRequestDto(String user, String email, String bio,
                                                       String cat, String catDesc,
                                                       String ing, String rec, String recDesc) {
        TransactionTestRequestDto dto = new TransactionTestRequestDto();
        dto.setUserUsername(user);
        dto.setUserEmail(email);
        dto.setUserBio(bio);
        dto.setCategoryName(cat);
        dto.setCategoryDescription(catDesc);
        dto.setIngredientName(ing);
        dto.setRecipeTitle(rec);
        dto.setRecipeDescription(recDesc);
        return dto;
    }
}
