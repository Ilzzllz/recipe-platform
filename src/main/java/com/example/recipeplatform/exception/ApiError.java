package com.example.recipeplatform.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Standard API error response")
public class ApiError {

    @Schema(description = "Timestamp when the error was generated", example = "2026-04-30T02:39:22.8439546")
    private final LocalDateTime timestamp = LocalDateTime.now();
    @Schema(description = "HTTP status code", example = "400")
    private final int status;
    @Schema(description = "Human-readable error message", example = "Username already exists")
    private final String error;

    public ApiError(int status, String error) {
        this.status = status;
        this.error = error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}
