package com.example.recipeplatform.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Standard API error response")
public class ApiError {

    @Schema(description = "Timestamp when the error was generated", example = "2026-04-30T02:39:22.8439546")
    private final LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "HTTP status code", example = "400")
    private final int status;

    @Schema(description = "HTTP error name", example = "Bad Request")
    private final String error;

    @Schema(description = "Human-readable error message", example = "Username already exists")
    private final String message;

    @Schema(description = "Request path where the error happened", example = "/api/users")
    private final String path;

    @Schema(description = "Optional structured details for validation or demo diagnostics")
    private final Map<String, Object> details;

    public ApiError(int status, String error, String message, String path, Map<String, Object> details) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.details = details;
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

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
