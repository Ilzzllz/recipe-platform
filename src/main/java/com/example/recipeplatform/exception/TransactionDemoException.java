package com.example.recipeplatform.exception;

public class TransactionDemoException extends RuntimeException {

    private final String scenario;
    private final String marker;

    public TransactionDemoException(String scenario, String marker, String message) {
        super(message);
        this.scenario = scenario;
        this.marker = marker;
    }

    public String getScenario() {
        return scenario;
    }

    public String getMarker() {
        return marker;
    }
}
