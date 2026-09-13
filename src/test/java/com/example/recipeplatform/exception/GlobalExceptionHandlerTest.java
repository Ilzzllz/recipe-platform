package com.example.recipeplatform.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestExceptionController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 404 with unified ApiError on NotFoundException")
    void handleNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Item not found"))
                .andExpect(jsonPath("$.path").value("/test/not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should return 400 with fieldErrors in details on @Valid body validation failure")
    void handleMethodArgumentNotValidException() throws Exception {
        mockMvc.perform(post("/test/valid-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed for request body."))
                .andExpect(jsonPath("$.path").value("/test/valid-body"))
                .andExpect(jsonPath("$.details.fieldErrors.name").value("Name must not be blank"));
    }

    @Test
    @DisplayName("Should return 400 on IllegalArgumentException")
    void handleIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid argument supplied"))
                .andExpect(jsonPath("$.path").value("/test/illegal-argument"));
    }

    @Test
    @DisplayName("Should return 400 when required request parameter is missing")
    void handleMissingRequestParam() throws Exception {
        mockMvc.perform(get("/test/param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Required request parameter 'requiredParam' for method parameter type String is not present")));
    }

    @Test
    @DisplayName("Should return 409 on DataIntegrityViolationException")
    void handleDataIntegrityViolationException() throws Exception {
        mockMvc.perform(get("/test/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("Operation violates database constraints: duplicate key error")));
    }

    @Test
    @DisplayName("Should return 500 with unified ApiError on uncaught generic Exception")
    void handleGenericException() throws Exception {
        mockMvc.perform(get("/test/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Internal server error."))
                .andExpect(jsonPath("$.path").value("/test/generic-error"));
    }

    public static class TestRequestBody {
        @NotBlank(message = "Name must not be blank")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @RestController
    static class TestExceptionController {

        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new NotFoundException("Item not found");
        }

        @PostMapping("/test/valid-body")
        public void validateBody(@Valid @RequestBody TestRequestBody body) {
        }

        @GetMapping("/test/illegal-argument")
        public void throwIllegalArgument() {
            throw new IllegalArgumentException("Invalid argument supplied");
        }

        @GetMapping("/test/param")
        public void requireParam(@RequestParam String requiredParam) {
        }

        @GetMapping("/test/data-integrity")
        public void throwDataIntegrity() {
            throw new DataIntegrityViolationException("Database constraint failed", new RuntimeException("duplicate key error"));
        }

        @GetMapping("/test/generic-error")
        public void throwGeneric() {
            throw new RuntimeException("Unexpected runtime failure");
        }
    }
}
