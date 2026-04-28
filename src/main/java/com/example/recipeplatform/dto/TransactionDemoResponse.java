package com.example.recipeplatform.dto;

import java.util.Map;

public class TransactionDemoResponse {

    private String scenario;
    private String marker;
    private String message;
    private Map<String, Long> persistedRecords;

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Long> getPersistedRecords() {
        return persistedRecords;
    }

    public void setPersistedRecords(Map<String, Long> persistedRecords) {
        this.persistedRecords = persistedRecords;
    }
}
