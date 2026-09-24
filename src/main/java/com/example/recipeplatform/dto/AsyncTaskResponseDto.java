package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Response containing status and result of an asynchronous background task")
public class AsyncTaskResponseDto {

    @Schema(description = "Unique task identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID taskId;

    @Schema(description = "Current execution status", example = "IN_PROGRESS")
    private AsyncTaskStatus status;

    @Schema(description = "Timestamp when the task was initiated")
    private LocalDateTime startedAt;

    @Schema(description = "Timestamp when the task was finished (null if still running)")
    private LocalDateTime completedAt;

    @Schema(description = "Human-readable progress or summary description", example = "Calculating nutrition from stored ingredient data for 3 ingredients...")
    private String message;

    @Schema(description = "Final result of the asynchronous computation (available when status is COMPLETED)")
    private NutritionReportDto result;
}
