package com.example.recipeplatform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CookingStepDto {

    private Long id;
    private Integer stepOrder;
    private String description;
    private Long recipeId;
}
