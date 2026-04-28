package com.example.recipeplatform.controller;

import com.example.recipeplatform.dto.CategoryDto;
import com.example.recipeplatform.dto.IngredientDto;
import com.example.recipeplatform.dto.UserDto;
import com.example.recipeplatform.service.ReferenceDataService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    public ReferenceDataController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping("/users")
    public List<UserDto> getUsers() {
        return referenceDataService.getUsers();
    }

    @GetMapping("/users/{id}")
    public UserDto getUser(@PathVariable Long id) {
        return referenceDataService.getUser(id);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserDto dto) {
        return referenceDataService.createUser(dto);
    }

    @PutMapping("/users/{id}")
    public UserDto updateUser(@PathVariable Long id, @Valid @RequestBody UserDto dto) {
        return referenceDataService.updateUser(id, dto);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        referenceDataService.deleteUser(id);
    }

    @GetMapping("/categories")
    public List<CategoryDto> getCategories() {
        return referenceDataService.getCategories();
    }

    @GetMapping("/categories/{id}")
    public CategoryDto getCategory(@PathVariable Long id) {
        return referenceDataService.getCategory(id);
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody CategoryDto dto) {
        return referenceDataService.createCategory(dto);
    }

    @PutMapping("/categories/{id}")
    public CategoryDto updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        return referenceDataService.updateCategory(id, dto);
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        referenceDataService.deleteCategory(id);
    }

    @GetMapping("/ingredients")
    public List<IngredientDto> getIngredients() {
        return referenceDataService.getIngredients();
    }

    @GetMapping("/ingredients/{id}")
    public IngredientDto getIngredient(@PathVariable Long id) {
        return referenceDataService.getIngredient(id);
    }

    @PostMapping("/ingredients")
    @ResponseStatus(HttpStatus.CREATED)
    public IngredientDto createIngredient(@Valid @RequestBody IngredientDto dto) {
        return referenceDataService.createIngredient(dto);
    }

    @PutMapping("/ingredients/{id}")
    public IngredientDto updateIngredient(@PathVariable Long id, @Valid @RequestBody IngredientDto dto) {
        return referenceDataService.updateIngredient(id, dto);
    }

    @DeleteMapping("/ingredients/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIngredient(@PathVariable Long id) {
        referenceDataService.deleteIngredient(id);
    }
}
