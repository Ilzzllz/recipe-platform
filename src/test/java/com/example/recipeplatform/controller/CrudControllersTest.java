package com.example.recipeplatform.controller;

import com.example.recipeplatform.dto.CategoryCreateDto;
import com.example.recipeplatform.dto.CategoryDto;
import com.example.recipeplatform.dto.CookingStepCreateDto;
import com.example.recipeplatform.dto.CookingStepDto;
import com.example.recipeplatform.dto.IngredientCreateDto;
import com.example.recipeplatform.dto.IngredientDto;
import com.example.recipeplatform.dto.UserCreateDto;
import com.example.recipeplatform.dto.UserDto;
import com.example.recipeplatform.service.CategoryService;
import com.example.recipeplatform.service.CookingStepService;
import com.example.recipeplatform.service.IngredientService;
import com.example.recipeplatform.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrudControllersTest {

    @Mock
    private UserService userService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private IngredientService ingredientService;
    @Mock
    private CookingStepService cookingStepService;

    @Test
    void userControllerDelegatesToService() {
        UserController controller = new UserController(userService);
        UserDto dto = new UserDto();
        UserCreateDto request = new UserCreateDto();
        when(userService.findAll()).thenReturn(List.of(dto));
        when(userService.getById(1L)).thenReturn(dto);
        when(userService.create(request)).thenReturn(dto);
        when(userService.update(1L, request)).thenReturn(dto);

        assertThat(controller.getAll()).containsExactly(dto);
        assertThat(controller.getById(1L)).isSameAs(dto);
        assertThat(controller.create(request)).isSameAs(dto);
        assertThat(controller.update(1L, request)).isSameAs(dto);
        controller.delete(1L);

        verify(userService).delete(1L);
    }

    @Test
    void categoryControllerDelegatesToService() {
        CategoryController controller = new CategoryController(categoryService);
        CategoryDto dto = new CategoryDto();
        CategoryCreateDto request = new CategoryCreateDto();
        when(categoryService.findAll()).thenReturn(List.of(dto));
        when(categoryService.getById(1L)).thenReturn(dto);
        when(categoryService.create(request)).thenReturn(dto);
        when(categoryService.update(1L, request)).thenReturn(dto);

        assertThat(controller.getAll()).containsExactly(dto);
        assertThat(controller.getById(1L)).isSameAs(dto);
        assertThat(controller.create(request)).isSameAs(dto);
        assertThat(controller.update(1L, request)).isSameAs(dto);
        controller.delete(1L);

        verify(categoryService).delete(1L);
    }

    @Test
    void ingredientControllerDelegatesToService() {
        IngredientController controller = new IngredientController(ingredientService);
        IngredientDto dto = new IngredientDto();
        IngredientCreateDto request = new IngredientCreateDto();
        when(ingredientService.findAll()).thenReturn(List.of(dto));
        when(ingredientService.getById(1L)).thenReturn(dto);
        when(ingredientService.create(request)).thenReturn(dto);
        when(ingredientService.update(1L, request)).thenReturn(dto);

        assertThat(controller.getAll()).containsExactly(dto);
        assertThat(controller.getById(1L)).isSameAs(dto);
        assertThat(controller.create(request)).isSameAs(dto);
        assertThat(controller.update(1L, request)).isSameAs(dto);
        controller.delete(1L);

        verify(ingredientService).delete(1L);
    }

    @Test
    void cookingStepControllerDelegatesToService() {
        CookingStepController controller = new CookingStepController(cookingStepService);
        CookingStepDto dto = new CookingStepDto();
        CookingStepCreateDto request = new CookingStepCreateDto();
        when(cookingStepService.findAll()).thenReturn(List.of(dto));
        when(cookingStepService.findById(1L)).thenReturn(dto);
        when(cookingStepService.create(request)).thenReturn(dto);
        when(cookingStepService.update(1L, request)).thenReturn(dto);

        assertThat(controller.getAll()).containsExactly(dto);
        assertThat(controller.getById(1L)).isSameAs(dto);
        assertThat(controller.create(request)).isSameAs(dto);
        assertThat(controller.update(1L, request)).isSameAs(dto);
        controller.delete(1L);

        verify(cookingStepService).delete(1L);
    }
}
