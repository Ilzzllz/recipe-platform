package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Results of the ExecutorService race condition demonstration")
public class RaceConditionDemoResultDto {

    @Schema(description = "Number of concurrent threads launched", example = "50")
    private int threadCount;

    @Schema(description = "Number of increments per thread", example = "100")
    private int incrementsPerThread;

    @Schema(description = "Total expected operations (threadCount * incrementsPerThread)", example = "5000")
    private long expectedTotal;

    @Schema(description = "Result of AtomicLong counter (always matches expectedTotal)", example = "5000")
    private long atomicCounterResult;

    @Schema(description = "Result of synchronized counter (always matches expectedTotal)", example = "5000")
    private long synchronizedCounterResult;

    @Schema(description = "Result of raw int counter (less than expectedTotal due to race condition)", example = "4312")
    private long unsafeCounterResult;

    @Schema(description = "Lost increments due to race condition", example = "688")
    private long lostUpdates;

    @Schema(description = "Execution time in milliseconds", example = "45")
    private long executionTimeMs;
}
