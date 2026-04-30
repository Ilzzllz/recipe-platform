package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "User create or update request")
public class UserCreateDto {

    @NotBlank
    @Schema(description = "Unique username", example = "anna")
    private String username;

    @Email
    @NotBlank
    @Schema(description = "Unique email", example = "anna@example.com")
    private String email;

    @Schema(description = "Short user bio", example = "Home cook who shares family recipes")
    private String bio;
}
