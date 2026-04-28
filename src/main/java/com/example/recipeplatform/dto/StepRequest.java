package com.example.recipeplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StepRequest {

    @NotNull
    private Integer stepOrder;

    @NotBlank
    private String description;
}
