package com.example.recipeplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Comparison of thread-safe and non-thread-safe view counters")
public class CounterStatsDto {

    @Schema(description = "Thread-safe counter value using AtomicLong", example = "1000")
    private long atomicCounter;

    @Schema(description = "Thread-safe counter value using synchronized method", example = "1000")
    private long synchronizedCounter;

    @Schema(description = "Non-thread-safe counter value using raw int increment", example = "872")
    private long unsafeCounter;

    @Schema(description = "Number of lost updates due to race condition (atomic - unsafe)", example = "128")
    private long lostUpdates;

    @Schema(description = "Indicates whether a race condition was detected", example = "true")
    private boolean raceConditionObserved;
}
