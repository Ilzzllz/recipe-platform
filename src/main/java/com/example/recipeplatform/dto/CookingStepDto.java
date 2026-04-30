package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Cooking step response")
public class CookingStepDto extends CookingStepCreateDto {

    @Schema(description = "Cooking step id", example = "25")
    private Long id;
}
