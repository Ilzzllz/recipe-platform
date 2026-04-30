package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "User response")
public class UserDto extends UserCreateDto {

    @Schema(description = "User id", example = "1")
    private Long id;
}
