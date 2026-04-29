package com.example.recipeplatform.exception;

import com.example.recipeplatform.dto.TransactionDemoResponse;
import com.example.recipeplatform.repository.CategoryRepository;
import com.example.recipeplatform.repository.CookingStepRepository;
import com.example.recipeplatform.repository.IngredientRepository;
import com.example.recipeplatform.repository.RecipeRepository;
import com.example.recipeplatform.repository.UserRepository;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final CookingStepRepository cookingStepRepository;

    public GlobalExceptionHandler(UserRepository userRepository,
                                  CategoryRepository categoryRepository,
                                  IngredientRepository ingredientRepository,
                                  RecipeRepository recipeRepository,
                                  CookingStepRepository cookingStepRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
        this.cookingStepRepository = cookingStepRepository;
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(HttpStatus.NOT_FOUND.value(), exception.getMessage()));
    }

    @ExceptionHandler(TransactionDemoException.class)
    public ResponseEntity<TransactionDemoResponse> handleTransactionDemo(TransactionDemoException exception) {
        TransactionDemoResponse response = new TransactionDemoResponse();
        response.setScenario(exception.getScenario());
        response.setMarker(exception.getMarker());
        response.setMessage(exception.getMessage());
        response.setPersistedRecords(Map.of(
                "users", userRepository.countByUsernameStartingWith(exception.getMarker()),
                "categories", categoryRepository.countByNameStartingWith(exception.getMarker()),
                "ingredients", ingredientRepository.countByNameStartingWith(exception.getMarker()),
                "recipes", recipeRepository.countByTitleStartingWith(exception.getMarker()),
                "cookingSteps", cookingStepRepository.countByDescriptionStartingWith(exception.getMarker())
        ));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            IllegalArgumentException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(Exception exception) {
        String message = exception.getMessage();
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            message = methodArgumentNotValidException.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        }
        return ResponseEntity.badRequest().body(new ApiError(HttpStatus.BAD_REQUEST.value(), message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrityViolation(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(HttpStatus.CONFLICT.value(),
                        "Operation violates database constraints: " + exception.getMostSpecificCause().getMessage()));
    }
}
