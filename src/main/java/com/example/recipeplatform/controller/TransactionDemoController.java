package com.example.recipeplatform.controller;

import com.example.recipeplatform.dto.TransactionDemoResponse;
import com.example.recipeplatform.dto.TransactionTestRequestDto;
import com.example.recipeplatform.exception.TransactionDemoException;
import com.example.recipeplatform.repository.CategoryRepository;
import com.example.recipeplatform.repository.IngredientRepository;
import com.example.recipeplatform.repository.RecipeRepository;
import com.example.recipeplatform.repository.UserRepository;
import com.example.recipeplatform.service.RecipeTransactionScenarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demo/transaction")
@Tag(name = "Transaction Demo", description = "Demonstrates @Transactional vs non-transactional behavior")
public class TransactionDemoController {

    private final RecipeTransactionScenarioService scenarioService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;

    public TransactionDemoController(RecipeTransactionScenarioService scenarioService,
                                     UserRepository userRepository,
                                     CategoryRepository categoryRepository,
                                     IngredientRepository ingredientRepository,
                                     RecipeRepository recipeRepository) {
        this.scenarioService = scenarioService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
    }

    @PostMapping("/without")
    @Operation(summary = "Run transaction demo WITHOUT @Transactional",
            description = "Saves User, Category, Ingredient, then fails on Recipe. " +
                    "Without @Transactional, previous entities remain in DB.")
    public TransactionDemoResponse demoWithoutTransaction(@Valid @RequestBody TransactionTestRequestDto request) {
        String marker = String.valueOf(Instant.now().toEpochMilli());
        try {
            scenarioService.saveWithoutTransactional(request, marker);
            return buildResponse("Without @Transactional", marker,
                    "Unexpected: method completed without exception", null);
        } catch (TransactionDemoException e) {
            return buildResponse(e.getScenario(), e.getMarker(), e.getMessage(), marker);
        }
    }

    @PostMapping("/with")
    @Operation(summary = "Run transaction demo WITH @Transactional",
            description = "Saves User, Category, Ingredient, then fails on Recipe. " +
                    "With @Transactional, all entities are rolled back.")
    public TransactionDemoResponse demoWithTransaction(@Valid @RequestBody TransactionTestRequestDto request) {
        String marker = String.valueOf(Instant.now().toEpochMilli());
        try {
            scenarioService.saveWithTransactional(request, marker);
            return buildResponse("With @Transactional", marker,
                    "Unexpected: method completed without exception", null);
        } catch (TransactionDemoException e) {
            return buildResponse(e.getScenario(), e.getMarker(), e.getMessage(), marker);
        }
    }

    private TransactionDemoResponse buildResponse(String scenario, String marker, String message, String usedMarker) {
        String effectiveMarker = (usedMarker != null) ? usedMarker : marker;
        TransactionDemoResponse response = new TransactionDemoResponse();
        response.setScenario(scenario);
        response.setMarker(effectiveMarker);
        response.setMessage(message);

        Map<String, Long> records = new LinkedHashMap<>();
        records.put("users", userRepository.countByUsernameStartingWith(effectiveMarker));
        records.put("categories", categoryRepository.countByNameStartingWith(effectiveMarker));
        records.put("ingredients", ingredientRepository.countByNameStartingWith(effectiveMarker));
        records.put("recipes", recipeRepository.countByTitleStartingWith(effectiveMarker));
        response.setPersistedRecords(records);

        return response;
    }
}