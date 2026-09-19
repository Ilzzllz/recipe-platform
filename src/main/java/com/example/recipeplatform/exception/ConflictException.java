package com.example.recipeplatform.exception;

public class ConflictException extends IllegalArgumentException {

    public ConflictException(String message) {
        super(message);
    }
}
