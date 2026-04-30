package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Minimal author reference inside recipe response")
public class AuthorReferenceDto {

    @Schema(description = "Author id", example = "1")
    private Long id;

    @Schema(description = "Author username", example = "anna")
    private String username;
}