package com.example.recipeplatform.service;

import com.example.recipeplatform.cache.RecipeQueryCacheService;
import com.example.recipeplatform.dto.RecipeCreateDto;
import com.example.recipeplatform.dto.RecipeIngredientCreateDto;
import com.example.recipeplatform.dto.RecipeStepCreateDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.mapper.CookingStepMapper;
import com.example.recipeplatform.mapper.RecipeMapper;
import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.model.CookingStep;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.RecipeIngredient;
import com.example.recipeplatform.model.User;
import com.example.recipeplatform.repository.CategoryRepository;
import com.example.recipeplatform.repository.IngredientRepository;
import com.example.recipeplatform.repository.RecipeRepository;
import com.example.recipeplatform.repository.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecipeServiceIngredientsTest {

    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private IngredientRepository ingredientRepository;
    @Mock
    private RecipeMapper recipeMapper;
    @Mock
    private CookingStepMapper cookingStepMapper;
    @Mock
    private EntityManagerFactory entityManagerFactory;
    @Mock
    private RecipeQueryCacheService recipeQueryCacheService;

    private RecipeService recipeService;
    private final Recipe recipe = new Recipe();

    @BeforeEach
    void setUp() {
        recipeService = new RecipeService(recipeRepository, userRepository, categoryRepository,
                ingredientRepository, recipeMapper, cookingStepMapper, entityManagerFactory,
                recipeQueryCacheService);
        User user = new User();
        user.setId(1L);
        Category category = new Category();
        category.setId(2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(recipeMapper.toEntity(any())).thenReturn(recipe);
        when(cookingStepMapper.toEntity(any(RecipeStepCreateDto.class))).thenReturn(new CookingStep());
        when(recipeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void explicitQuantitiesNormalizeEveryKnownUnit() {
        List<String> units = new ArrayList<>(List.of(
                "g", "гр", "грамм", "грамма", "граммов",
                "ml", "миллилитр", "миллилитров",
                "piece", "pieces", "pcs", "штука", "штуки",
                "  КГ  "));
        units.add(null);
        List<RecipeIngredientCreateDto> items = new ArrayList<>();
        for (int i = 0; i < units.size(); i++) {
            items.add(item((long) i + 1, "1.5", units.get(i)));
        }
        when(ingredientRepository.findById(anyLong()))
                .thenAnswer(invocation -> Optional.of(ingredient(invocation.getArgument(0))));

        RecipeCreateDto request = request();
        request.setRecipeIngredients(items);
        recipeService.create(request);

        ArgumentCaptor<Recipe> saved = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).save(saved.capture());
        assertThat(saved.getValue().getRecipeIngredientDetails())
                .extracting(RecipeIngredient::getUnit)
                .containsExactly("г", "г", "г", "г", "г", "мл", "мл", "мл",
                        "шт", "шт", "шт", "шт", "шт", "кг", "г");
        assertThat(saved.getValue().getIngredients()).hasSize(units.size());
        verify(ingredientRepository, never()).findAllById(any());
    }

    static Stream<Arguments> invalidItems() {
        List<RecipeIngredientCreateDto> nullItem = new ArrayList<>();
        nullItem.add(null);
        return Stream.of(
                Arguments.of(nullItem),
                Arguments.of(List.of(item(null, "1", "г"))),
                Arguments.of(List.of(item(1L, null, "г"))),
                Arguments.of(List.of(item(1L, "0", "г"))),
                Arguments.of(List.of(item(1L, "-2", "г"))));
    }

    @ParameterizedTest
    @MethodSource("invalidItems")
    void invalidQuantityItemsAreRejected(List<RecipeIngredientCreateDto> items) {
        RecipeCreateDto request = request();
        request.setRecipeIngredients(items);

        assertThatThrownBy(() -> recipeService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("больше нуля");
    }

    @Test
    void unknownIngredientInQuantitiesIsNotFound() {
        when(ingredientRepository.findById(5L)).thenReturn(Optional.empty());
        RecipeCreateDto request = request();
        request.setRecipeIngredients(List.of(item(5L, "10", "г")));

        assertThatThrownBy(() -> recipeService.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("One or more ingredients were not found");
    }

    @Test
    void recipeWithoutAnyIngredientsIsRejected() {
        RecipeCreateDto withNullIds = request();
        withNullIds.setRecipeIngredients(List.of());
        withNullIds.setIngredientIds(null);
        RecipeCreateDto withEmptyIds = request();
        withEmptyIds.setIngredientIds(Set.of());

        assertThatThrownBy(() -> recipeService.create(withNullIds))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Recipe must contain at least one ingredient");
        assertThatThrownBy(() -> recipeService.create(withEmptyIds))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Recipe must contain at least one ingredient");
    }

    @Test
    void legacyIdsReportTheExactMissingIngredient() {
        Ingredient first = ingredient(3L);
        when(ingredientRepository.findAllById(any())).thenReturn(List.of(first));
        when(ingredientRepository.findById(3L)).thenReturn(Optional.of(first));
        when(ingredientRepository.findById(4L)).thenReturn(Optional.empty());
        RecipeCreateDto request = request();
        request.setIngredientIds(new LinkedHashSet<>(List.of(3L, 4L)));

        assertThatThrownBy(() -> recipeService.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("One or more ingredients were not found (Ingredient with id 4 was not found)");
    }

    @Test
    void legacyIdsRecoverWhenBatchLookupMissesExistingIngredients() {
        Ingredient first = ingredient(3L);
        Ingredient second = ingredient(4L);
        when(ingredientRepository.findAllById(any())).thenReturn(List.of(first));
        when(ingredientRepository.findById(3L)).thenReturn(Optional.of(first));
        when(ingredientRepository.findById(4L)).thenReturn(Optional.of(second));
        RecipeCreateDto request = request();
        request.setIngredientIds(new LinkedHashSet<>(List.of(3L, 4L)));

        recipeService.create(request);

        assertThat(recipe.getRecipeIngredientDetails())
                .extracting(detail -> detail.getIngredient().getId(), RecipeIngredient::getUnit)
                .containsExactly(tuple(3L, "г"), tuple(4L, "г"));
        assertThat(recipe.getRecipeIngredientDetails())
                .allSatisfy(detail -> assertThat(detail.getQuantity()).isEqualByComparingTo("100"));
    }

    @Test
    void bulkKeepsValuesAlreadyMappedIntoTheRecipeAndSteps() {
        recipe.setTitle("Mapped title");
        recipe.setDescription("Mapped description");
        CookingStep mappedStep = new CookingStep();
        mappedStep.setStepOrder(1);
        when(cookingStepMapper.toEntity(any(RecipeStepCreateDto.class))).thenReturn(mappedStep);
        when(ingredientRepository.findAllById(any())).thenReturn(List.of(ingredient(3L)));
        when(recipeRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        RecipeCreateDto request = request();
        request.setIngredientIds(Set.of(3L));

        recipeService.createBulk(List.of(request));

        assertThat(recipe.getTitle()).isEqualTo("Mapped title");
        assertThat(recipe.getDescription()).isEqualTo("Mapped description");
        assertThat(recipe.getSteps()).containsExactly(mappedStep);
        verify(recipeMapper).updateEntity(recipe, request);
        verify(recipeQueryCacheService).invalidateAll();
    }

    private static RecipeIngredientCreateDto item(Long ingredientId, String quantity, String unit) {
        RecipeIngredientCreateDto item = new RecipeIngredientCreateDto();
        item.setIngredientId(ingredientId);
        item.setQuantity(quantity == null ? null : new BigDecimal(quantity));
        item.setUnit(unit);
        return item;
    }

    private static Ingredient ingredient(Long id) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(id);
        return ingredient;
    }

    private static RecipeCreateDto request() {
        RecipeStepCreateDto step = new RecipeStepCreateDto();
        step.setStepOrder(1);
        step.setDescription("Cook");
        RecipeCreateDto request = new RecipeCreateDto();
        request.setTitle("Борщ");
        request.setAuthorId(1L);
        request.setCategoryId(2L);
        request.setSteps(List.of(step));
        return request;
    }
}
