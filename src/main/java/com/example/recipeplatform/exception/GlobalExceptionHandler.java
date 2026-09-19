package com.example.recipeplatform.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException exception, HttpServletRequest request) {
        logger.warn("HTTP 404 [NOT_FOUND] on {} {}: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage());
        return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(Exception exception, HttpServletRequest request) {
        logger.warn("HTTP 400 [BAD_REQUEST] on {} {}: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException exception, HttpServletRequest request) {
        logger.warn("HTTP 409 [CONFLICT] on {} {}: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage());
        return buildError(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                 HttpServletRequest request) {
        Map<String, Object> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() == null
                                ? "Invalid value"
                                : fieldError.getDefaultMessage(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
        logger.warn("HTTP 400 [BAD_REQUEST] Validation failed on {} {}: {}",
                request.getMethod(), request.getRequestURI(), fieldErrors);
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("fieldErrors", fieldErrors);
        return buildError(HttpStatus.BAD_REQUEST, "Validation failed for request body.", request, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception,
                                                              HttpServletRequest request) {
        Map<String, Object> violations = exception.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
        logger.warn("HTTP 400 [BAD_REQUEST] Constraint violation on {} {}: {}",
                request.getMethod(), request.getRequestURI(), violations);
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("violations", violations);
        return buildError(HttpStatus.BAD_REQUEST, "Validation failed for request parameters.", request, details);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrityViolation(DataIntegrityViolationException exception,
                                                             HttpServletRequest request) {
        String causeMessage = exception.getMostSpecificCause() != null
                ? exception.getMostSpecificCause().getMessage()
                : exception.getMessage();
        logger.error("HTTP 409 [CONFLICT] on {} {}: {}",
                request.getMethod(), request.getRequestURI(), causeMessage);
        return buildError(HttpStatus.CONFLICT,
                "Operation violates database constraints: " + causeMessage,
                request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(NoResourceFoundException exception,
                                                          HttpServletRequest request) {
        logger.warn("HTTP 404 [NOT_FOUND] on {} {}: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage());
        return buildError(HttpStatus.NOT_FOUND, "Resource not found: " + request.getRequestURI(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception exception, HttpServletRequest request) {
        logger.error("HTTP 500 [INTERNAL_SERVER_ERROR] on {} {}: {} (Exception: {})",
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage(),
                exception.getClass().getSimpleName());
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error.", request);
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status,
                                                String message,
                                                HttpServletRequest request) {
        return buildError(status, message, request, null);
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status,
                                                String message,
                                                HttpServletRequest request,
                                                Map<String, Object> details) {
        ApiError error = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(status).body(error);
    }
}
