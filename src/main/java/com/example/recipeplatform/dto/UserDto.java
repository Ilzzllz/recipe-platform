package com.example.recipeplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    private Long id;

    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    private String bio;
}
