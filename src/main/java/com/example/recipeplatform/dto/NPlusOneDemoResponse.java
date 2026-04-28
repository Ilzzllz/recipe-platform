package com.example.recipeplatform.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NPlusOneDemoResponse {

    private String scenario;
    private long sqlStatements;
    private long recipesLoaded;
    private List<RecipeDto> recipes;
}
