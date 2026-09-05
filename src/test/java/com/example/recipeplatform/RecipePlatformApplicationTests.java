package com.example.recipeplatform;

import com.example.recipeplatform.cache.CacheKey;
import com.example.recipeplatform.cache.RecipeQueryCacheService;
import com.example.recipeplatform.dto.RecipeCreateDto;
import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.dto.RecipeFilterDto;
import com.example.recipeplatform.dto.RecipeStepCreateDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.model.Category;
import com.example.recipeplatform.model.Ingredient;
import com.example.recipeplatform.model.User;
import com.example.recipeplatform.repository.CategoryRepository;
import com.example.recipeplatform.repository.IngredientRepository;
import com.example.recipeplatform.repository.RecipeRepository;
import com.example.recipeplatform.repository.UserRepository;
import com.example.recipeplatform.service.RecipeService;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.boot.test.context.SpringBootTest;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.seed.enabled=true")
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
class RecipePlatformApplicationTests {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private RecipeQueryCacheService recipeQueryCacheService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void clearCache() {
        recipeQueryCacheService.invalidateAll();
    }

    @Test
    void contextLoads() {
        // Spring fails this test during context bootstrap if the application cannot start.
    }

    @Test
    void cacheKeyShouldUseNormalizedCompositeKey() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"));

        CacheKey first = CacheKey.from("jpql", " Anna ", " Soups ", pageable);
        CacheKey second = CacheKey.from("jpql", "anna", "soups", pageable);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void jpqlFilterShouldAvoidNPlusOneAndUseCache() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"));
        Statistics statistics = statistics();

        statistics.clear();
        Page<RecipeFilterDto> firstCall = recipeService.findByAuthorAndCategoryJPQL("anna", "Soups", pageable);

        assertThat(firstCall.getContent()).isNotEmpty();
        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(2L);

        statistics.clear();
        Page<RecipeFilterDto> cachedCall = recipeService.findByAuthorAndCategoryJPQL("anna", "Soups", pageable);

        assertThat(cachedCall.getContent()).hasSameSizeAs(firstCall.getContent());
        assertThat(statistics.getPrepareStatementCount()).isZero();
    }

    @Test
    void nativeFilterShouldAvoidNPlusOneAndUseCache() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"));
        Statistics statistics = statistics();

        statistics.clear();
        Page<RecipeFilterDto> firstCall = recipeService.findByAuthorAndCategoryNative("anna", "Soups", pageable);

        assertThat(firstCall.getContent()).isNotEmpty();
        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(2L);

        statistics.clear();
        Page<RecipeFilterDto> cachedCall = recipeService.findByAuthorAndCategoryNative("anna", "Soups", pageable);

        assertThat(cachedCall.getContent()).hasSameSizeAs(firstCall.getContent());
        assertThat(statistics.getPrepareStatementCount()).isZero();
    }

    @Test
    void recipeMutationShouldInvalidateFilterCache() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"));
        Statistics statistics = statistics();

        recipeService.findByAuthorAndCategoryJPQL("anna", "Soups", pageable);
        statistics.clear();
        recipeService.findByAuthorAndCategoryJPQL("anna", "Soups", pageable);
        assertThat(statistics.getPrepareStatementCount()).isZero();

        User author = userRepository.findByUsernameIgnoreCase("anna").orElseThrow();
        Category category = categoryRepository.findByNameIgnoreCase("Soups").orElseThrow();
        List<Long> ingredientIds = ingredientRepository.findAll().stream()
                .map(Ingredient::getId)
                .limit(2)
                .toList();

        RecipeCreateDto request = new RecipeCreateDto();
        request.setTitle("cache_test_" + UUID.randomUUID());
        request.setDescription("Recipe used to verify cache invalidation");
        request.setAuthorId(author.getId());
        request.setCategoryId(category.getId());
        request.setIngredientIds(Set.copyOf(ingredientIds));
        request.setSteps(List.of(step(1, "Create a temporary recipe for cache invalidation testing")));

        RecipeDto createdRecipe = recipeService.create(request);
        try {
            statistics.clear();
            recipeService.findByAuthorAndCategoryJPQL("anna", "Soups", pageable);
            assertThat(statistics.getPrepareStatementCount()).isGreaterThan(0L);
        } finally {
            recipeService.delete(createdRecipe.getId());
        }
    }

    @Test
    void transactionalBulkShouldRollbackEveryRecipeWhenLaterItemFails() {
        RecipeCreateDto savedCandidate = bulkRequest("tx_bulk_" + UUID.randomUUID());
        RecipeCreateDto invalidCandidate = bulkRequest("tx_invalid_" + UUID.randomUUID());
        invalidCandidate.setIngredientIds(Set.of(Long.MAX_VALUE));

        assertThatThrownBy(() -> recipeService.createBulk(List.of(savedCandidate, invalidCandidate)))
                .isInstanceOf(NotFoundException.class);

        assertThat(recipeRepository.existsByTitleIgnoreCase(savedCandidate.getTitle())).isFalse();
    }

    @Test
    void bulkWithoutTransactionShouldKeepEarlierRecipeWhenLaterItemFails() {
        RecipeCreateDto savedCandidate = bulkRequest("no_tx_bulk_" + UUID.randomUUID());
        RecipeCreateDto invalidCandidate = bulkRequest("no_tx_invalid_" + UUID.randomUUID());
        invalidCandidate.setIngredientIds(Set.of(Long.MAX_VALUE));

        assertThatThrownBy(() -> recipeService.createBulkWithoutTransaction(List.of(savedCandidate, invalidCandidate)))
                .isInstanceOf(NotFoundException.class);

        assertThat(recipeRepository.existsByTitleIgnoreCase(savedCandidate.getTitle())).isTrue();
        recipeRepository.findAll().stream()
                .filter(recipe -> recipe.getTitle().equals(savedCandidate.getTitle()))
                .findFirst()
                .ifPresent(recipe -> recipeService.delete(recipe.getId()));
    }

    @Test
    void invalidRequestShouldReturnUnifiedApiError() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "email": "not-an-email",
                                  "bio": "demo"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed for request body."))
                .andExpect(jsonPath("$.path").value("/api/users"))
                .andExpect(jsonPath("$.details.fieldErrors.username").exists())
                .andExpect(jsonPath("$.details.fieldErrors.email").exists());
    }

    @Test
    void openApiSpecShouldBeAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Recipe Platform API"))
                .andExpect(jsonPath("$.paths['/api/recipes/filter/jpql']").exists());
    }

    private Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    private RecipeStepCreateDto step(int order, String description) {
        RecipeStepCreateDto step = new RecipeStepCreateDto();
        step.setStepOrder(order);
        step.setDescription(description);
        return step;
    }

    private RecipeCreateDto bulkRequest(String title) {
        User author = userRepository.findByUsernameIgnoreCase("anna").orElseThrow();
        Category category = categoryRepository.findByNameIgnoreCase("Soups").orElseThrow();
        Long ingredientId = ingredientRepository.findAll().stream()
                .map(Ingredient::getId)
                .findFirst()
                .orElseThrow();

        RecipeCreateDto request = new RecipeCreateDto();
        request.setTitle(title);
        request.setDescription("Recipe used to demonstrate bulk transaction behavior");
        request.setAuthorId(author.getId());
        request.setCategoryId(category.getId());
        request.setIngredientIds(Set.of(ingredientId));
        request.setSteps(List.of(step(1, "Prepare bulk recipe")));
        return request;
    }
}
