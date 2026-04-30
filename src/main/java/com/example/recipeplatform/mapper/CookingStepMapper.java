package com.example.recipeplatform.mapper;

import com.example.recipeplatform.dto.CookingStepCreateDto;
import com.example.recipeplatform.dto.CookingStepDto;
import com.example.recipeplatform.dto.RecipeStepCreateDto;
import com.example.recipeplatform.model.CookingStep;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CookingStepMapper {

    public CookingStepDto toDto(CookingStep step) {
        if (step == null) {
            return null;
        }

        CookingStepDto dto = new CookingStepDto();
        dto.setId(step.getId());
        dto.setRecipeId(step.getRecipe() == null ? null : step.getRecipe().getId());
        dto.setStepOrder(step.getStepOrder());
        dto.setDescription(step.getDescription());
        return dto;
    }

    public CookingStep toEntity(CookingStepCreateDto dto) {
        CookingStep step = new CookingStep();
        updateEntity(step, dto);
        return step;
    }

    public CookingStep toEntity(RecipeStepCreateDto dto) {
        CookingStep step = new CookingStep();
        step.setStepOrder(dto.getStepOrder());
        step.setDescription(dto.getDescription());
        return step;
    }

    public void updateEntity(CookingStep step, CookingStepCreateDto dto) {
        step.setStepOrder(dto.getStepOrder());
        step.setDescription(dto.getDescription());
    }

    public List<CookingStepDto> toDtoList(List<CookingStep> steps) {
        return steps.stream()
                .map(this::toDto)
                .toList();
    }
}
