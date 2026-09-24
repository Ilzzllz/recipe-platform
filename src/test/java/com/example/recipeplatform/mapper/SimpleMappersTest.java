package com.example.recipeplatform.mapper;

import com.example.recipeplatform.dto.CategoryCreateDto;
import com.example.recipeplatform.dto.CategoryDto;
import com.example.recipeplatform.dto.CookingStepCreateDto;
import com.example.recipeplatform.dto.CookingStepDto;
import com.example.recipeplatform.dto.IngredientCreateDto;
import com.example.recipeplatform.dto.IngredientDto;
import com.example.recipeplatform.dto.RecipeStepCreateDto;
import com.example.recipeplatform.dto.UserCreateDto;
import com.example.recipeplatform.dto.UserDto;
import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.model.CookingStep;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleMappersTest {

    private final CategoryMapper categoryMapper = new CategoryMapper();
    private final IngredientMapper ingredientMapper = new IngredientMapper();
    private final CookingStepMapper cookingStepMapper = new CookingStepMapper();
    private final UserMapper userMapper = new UserMapper();

    @Test
    void categoryMapperMapsBothDirections() {
        assertThat(categoryMapper.toDto(null)).isNull();

        CategoryCreateDto request = new CategoryCreateDto();
        request.setName("  супы  ");
        request.setDescription(" горячие   блюда ");
        Category category = categoryMapper.toEntity(request);
        category.setId(5L);

        assertThat(category.getName()).isEqualTo("Супы");
        assertThat(category.getDescription()).isEqualTo("Горячие блюда");

        List<CategoryDto> dtos = categoryMapper.toDtoList(List.of(category));
        assertThat(dtos).singleElement().satisfies(dto -> {
            assertThat(dto.getId()).isEqualTo(5L);
            assertThat(dto.getName()).isEqualTo("Супы");
            assertThat(dto.getDescription()).isEqualTo("Горячие блюда");
        });
    }

    @Test
    void ingredientMapperMapsBothDirections() {
        assertThat(ingredientMapper.toDto(null)).isNull();

        IngredientCreateDto request = new IngredientCreateDto();
        request.setName(" морковь ");
        request.setCaloriesPer100g(new BigDecimal("41"));
        request.setProteinsPer100g(new BigDecimal("0.9"));
        request.setFatsPer100g(new BigDecimal("0.2"));
        request.setCarbohydratesPer100g(new BigDecimal("9.6"));
        request.setGramsPerUnit(new BigDecimal("75"));
        Ingredient ingredient = ingredientMapper.toEntity(request);
        ingredient.setId(7L);

        List<IngredientDto> dtos = ingredientMapper.toDtoList(List.of(ingredient));
        assertThat(dtos).singleElement().satisfies(dto -> {
            assertThat(dto.getId()).isEqualTo(7L);
            assertThat(dto.getName()).isEqualTo("Морковь");
            assertThat(dto.getCaloriesPer100g()).isEqualByComparingTo("41");
            assertThat(dto.getProteinsPer100g()).isEqualByComparingTo("0.9");
            assertThat(dto.getFatsPer100g()).isEqualByComparingTo("0.2");
            assertThat(dto.getCarbohydratesPer100g()).isEqualByComparingTo("9.6");
            assertThat(dto.getGramsPerUnit()).isEqualByComparingTo("75");
        });
    }

    @Test
    void cookingStepMapperMapsBothDirections() {
        assertThat(cookingStepMapper.toDto(null)).isNull();

        CookingStepCreateDto request = new CookingStepCreateDto();
        request.setStepOrder(2);
        request.setDescription(" нарезать овощи ");
        CookingStep withoutRecipe = cookingStepMapper.toEntity(request);
        withoutRecipe.setId(1L);

        RecipeStepCreateDto recipeStep = new RecipeStepCreateDto();
        recipeStep.setStepOrder(1);
        recipeStep.setDescription(" помыть овощи ");
        CookingStep withRecipe = cookingStepMapper.toEntity(recipeStep);
        withRecipe.setId(2L);
        Recipe recipe = new Recipe();
        recipe.setId(9L);
        withRecipe.setRecipe(recipe);

        List<CookingStepDto> dtos = cookingStepMapper.toDtoList(List.of(withoutRecipe, withRecipe));

        assertThat(dtos.get(0).getRecipeId()).isNull();
        assertThat(dtos.get(0).getStepOrder()).isEqualTo(2);
        assertThat(dtos.get(0).getDescription()).isEqualTo("Нарезать овощи");
        assertThat(dtos.get(1).getId()).isEqualTo(2L);
        assertThat(dtos.get(1).getRecipeId()).isEqualTo(9L);
        assertThat(dtos.get(1).getDescription()).isEqualTo("Помыть овощи");
    }

    @Test
    void userMapperMapsBothDirections() {
        assertThat(userMapper.toDto(null)).isNull();

        UserCreateDto request = new UserCreateDto();
        request.setUsername(" anna ");
        request.setEmail("anna@example.com");
        request.setBio(" люблю  готовить ");
        User user = userMapper.toEntity(request);
        user.setId(3L);

        List<UserDto> dtos = userMapper.toDtoList(List.of(user));
        assertThat(dtos).singleElement().satisfies(dto -> {
            assertThat(dto.getId()).isEqualTo(3L);
            assertThat(dto.getUsername()).isEqualTo("Anna");
            assertThat(dto.getEmail()).isEqualTo("anna@example.com");
            assertThat(dto.getBio()).isEqualTo("Люблю готовить");
        });
    }
}
