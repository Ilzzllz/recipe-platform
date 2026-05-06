package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Schema(description = "Response for transaction demo endpoints")
public class TransactionDemoResponse {

    @Schema(description = "Demo scenario name", example = "Without @Transactional")
    private String scenario;

    @Schema(description = "Unique marker used to count persisted demo rows", example = "demo_plain_1777500000000")
    private String marker;

    @Schema(description = "Human-readable result message", example = "User, Category and Ingredient saved (partial commit), recipe NOT saved due to error.")
    private String message;

    @Schema(description = "How many records remained in the database after the scenario")
    private Map<String, Long> persistedRecords;
}