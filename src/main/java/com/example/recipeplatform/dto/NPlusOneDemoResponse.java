package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Response for N+1 demo endpoints")
public class NPlusOneDemoResponse {

    @Schema(description = "Demo scenario name", example = "Fetch join solution")
    private String scenario;
    @Schema(description = "Number of SQL statements prepared by Hibernate", example = "3")
    private long sqlStatements;
    @Schema(description = "How many recipes were loaded", example = "8")
    private long recipesLoaded;
    private List<RecipeDto> recipes;
}
