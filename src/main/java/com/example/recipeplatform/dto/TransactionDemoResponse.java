package com.example.recipeplatform.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TransactionDemoResponse {

    private String scenario;
    private String marker;
    private String message;
    private Map<String, Long> persistedRecords;
}
