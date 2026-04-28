package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.NPlusOneDemoResponse;
import com.example.recipeplatform.dto.RecipeDto;
import com.example.recipeplatform.dto.TransactionDemoResponse;
import com.example.recipeplatform.mapper.RecipeMapper;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.repository.CategoryRepository;
import com.example.recipeplatform.repository.CookingStepRepository;
import com.example.recipeplatform.repository.IngredientRepository;
import com.example.recipeplatform.repository.RecipeRepository;
import com.example.recipeplatform.repository.UserRepository;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManagerFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LabDemoService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final CookingStepRepository cookingStepRepository;
    private final RecipeMapper recipeMapper;
    private final EntityManagerFactory entityManagerFactory;
    private final RecipeTransactionScenarioService recipeTransactionScenarioService;

    public LabDemoService(RecipeRepository recipeRepository,
                          UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          IngredientRepository ingredientRepository,
                          CookingStepRepository cookingStepRepository,
                          RecipeMapper recipeMapper,
                          EntityManagerFactory entityManagerFactory,
                          RecipeTransactionScenarioService recipeTransactionScenarioService) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.cookingStepRepository = cookingStepRepository;
        this.recipeMapper = recipeMapper;
        this.entityManagerFactory = entityManagerFactory;
        this.recipeTransactionScenarioService = recipeTransactionScenarioService;
    }

    @Transactional(readOnly = true)
    public NPlusOneDemoResponse demonstrateNPlusOneProblem() {
        Statistics statistics = statistics();
        statistics.clear();

        List<Recipe> recipes = recipeRepository.findAll();
        List<RecipeDto> dtos = recipes.stream()
                .map(recipeMapper::toDto)
                .toList();

        return buildNPlusOneResponse("N+1 problem", dtos, statistics.getPrepareStatementCount());
    }

    @Transactional(readOnly = true)
    public NPlusOneDemoResponse demonstrateFetchJoinSolution() {
        Statistics statistics = statistics();
        statistics.clear();

        List<RecipeDto> dtos = recipeRepository.findAllWithDetails().stream()
                .map(recipeMapper::toDto)
                .toList();

        return buildNPlusOneResponse("Fetch join solution", dtos, statistics.getPrepareStatementCount());
    }

    public TransactionDemoResponse demonstratePartialSaveWithoutTransactional() {
        String marker = buildMarker("plain");
        try {
            recipeTransactionScenarioService.saveWithoutTransactional(marker);
            throw new IllegalStateException("Expected an exception but the demo finished successfully");
        } catch (RuntimeException exception) {
            return buildTransactionResponse("Without @Transactional", marker, exception.getMessage());
        }
    }

    public TransactionDemoResponse demonstrateRollbackWithTransactional() {
        String marker = buildMarker("tx");
        try {
            recipeTransactionScenarioService.saveWithTransactional(marker);
            throw new IllegalStateException("Expected an exception but the demo finished successfully");
        } catch (RuntimeException exception) {
            return buildTransactionResponse("With @Transactional", marker, exception.getMessage());
        }
    }

    private NPlusOneDemoResponse buildNPlusOneResponse(String scenario, List<RecipeDto> recipes, long statementCount) {
        NPlusOneDemoResponse response = new NPlusOneDemoResponse();
        response.setScenario(scenario);
        response.setSqlStatements(statementCount);
        response.setRecipesLoaded(recipes.size());
        response.setRecipes(recipes);
        return response;
    }

    private TransactionDemoResponse buildTransactionResponse(String scenario, String marker, String message) {
        Map<String, Long> persistedRecords = new LinkedHashMap<>();
        persistedRecords.put("users", userRepository.countByUsernameStartingWith(marker));
        persistedRecords.put("categories", categoryRepository.countByNameStartingWith(marker));
        persistedRecords.put("ingredients", ingredientRepository.countByNameStartingWith(marker));
        persistedRecords.put("recipes", recipeRepository.countByTitleStartingWith(marker));
        persistedRecords.put("cookingSteps", cookingStepRepository.countByDescriptionStartingWith(marker));

        TransactionDemoResponse response = new TransactionDemoResponse();
        response.setScenario(scenario);
        response.setMarker(marker);
        response.setMessage(message);
        response.setPersistedRecords(persistedRecords);
        return response;
    }

    private String buildMarker(String scenario) {
        return "demo_" + scenario + "_" + Instant.now().toEpochMilli();
    }

    private Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }
}
