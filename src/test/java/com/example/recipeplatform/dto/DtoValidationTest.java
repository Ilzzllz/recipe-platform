package com.example.recipeplatform.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("UserCreateDto should fail validation when username or email is blank or invalid")
    void testUserCreateDtoValidation() {
        UserCreateDto dto = new UserCreateDto();
        dto.setUsername("   ");
        dto.setEmail("invalid-email");

        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("CategoryCreateDto should fail validation when name is blank")
    void testCategoryCreateDtoValidation() {
        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setName("");

        Set<ConstraintViolation<CategoryCreateDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    @DisplayName("RecipeCreateDto should cascade validation to nested RecipeStepCreateDto")
    void testRecipeCreateDtoCascadeValidation() {
        RecipeCreateDto recipeDto = new RecipeCreateDto();
        recipeDto.setTitle("Valid Title");
        recipeDto.setDescription("Valid Description");
        recipeDto.setAuthorId(1L);
        recipeDto.setCategoryId(2L);
        recipeDto.setIngredientIds(Set.of(1L));

        RecipeStepCreateDto invalidStep = new RecipeStepCreateDto();
        // stepOrder is null, description is blank
        invalidStep.setDescription("");
        recipeDto.setSteps(List.of(invalidStep));

        Set<ConstraintViolation<RecipeCreateDto>> violations = validator.validate(recipeDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("steps[0].stepOrder"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("steps[0].description"));
    }
}
