package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.CategoryDto;
import com.example.recipeplatform.dto.IngredientDto;
import com.example.recipeplatform.dto.UserDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.User;
import com.example.recipeplatform.repository.CategoryRepository;
import com.example.recipeplatform.repository.IngredientRepository;
import com.example.recipeplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReferenceDataService {

    private static final String USER_WITH_ID_PREFIX = "User with id ";
    private static final String CATEGORY_WITH_ID_PREFIX = "Category with id ";
    private static final String INGREDIENT_WITH_ID_PREFIX = "Ingredient with id ";
    private static final String NOT_FOUND_SUFFIX = " was not found";

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;

    public ReferenceDataService(UserRepository userRepository,
                                CategoryRepository categoryRepository,
                                IngredientRepository ingredientRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto getUser(Long id) {
        return toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX)));
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toCategoryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategory(Long id) {
        return toCategoryDto(categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX)));
    }

    @Transactional(readOnly = true)
    public List<IngredientDto> getIngredients() {
        return ingredientRepository.findAll().stream()
                .map(this::toIngredientDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public IngredientDto getIngredient(Long id) {
        return toIngredientDto(ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(INGREDIENT_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX)));
    }

    @Transactional
    public UserDto createUser(UserDto dto) {
        if (userRepository.existsByUsernameIgnoreCase(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setBio(dto.getBio());
        return toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setBio(dto.getBio());
        return toUserDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
        userRepository.delete(user);
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Category already exists");
        }
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return toCategoryDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return toCategoryDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
        categoryRepository.delete(category);
    }

    @Transactional
    public IngredientDto createIngredient(IngredientDto dto) {
        if (ingredientRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Ingredient already exists");
        }
        Ingredient ingredient = new Ingredient();
        ingredient.setName(dto.getName());
        return toIngredientDto(ingredientRepository.save(ingredient));
    }

    @Transactional
    public IngredientDto updateIngredient(Long id, IngredientDto dto) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(INGREDIENT_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
        ingredient.setName(dto.getName());
        return toIngredientDto(ingredientRepository.save(ingredient));
    }

    @Transactional
    public void deleteIngredient(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(INGREDIENT_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
        ingredientRepository.delete(ingredient);
    }

    private UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        return dto;
    }

    private CategoryDto toCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }

    private IngredientDto toIngredientDto(Ingredient ingredient) {
        IngredientDto dto = new IngredientDto();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        return dto;
    }
}
