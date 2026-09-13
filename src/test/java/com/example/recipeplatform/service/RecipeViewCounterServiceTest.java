package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.CounterStatsDto;
import com.example.recipeplatform.dto.RaceConditionDemoResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeViewCounterServiceTest {

    private RecipeViewCounterService counterService;

    @BeforeEach
    void setUp() {
        counterService = new RecipeViewCounterService();
    }

    @Test
    @DisplayName("recordView should increment all three counters")
    void recordViewShouldIncrementAllCounters() {
        counterService.recordView();
        counterService.recordView();

        CounterStatsDto stats = counterService.getStats();

        assertThat(stats.getAtomicCounter()).isEqualTo(2);
        assertThat(stats.getSynchronizedCounter()).isEqualTo(2);
        assertThat(stats.getUnsafeCounter()).isEqualTo(2);
        assertThat(stats.getLostUpdates()).isZero();
        assertThat(stats.isRaceConditionObserved()).isFalse();
    }

    @Test
    @DisplayName("reset should reset all counters to zero")
    void resetShouldClearAllCounters() {
        counterService.recordView();
        counterService.reset();

        CounterStatsDto stats = counterService.getStats();

        assertThat(stats.getAtomicCounter()).isZero();
        assertThat(stats.getSynchronizedCounter()).isZero();
        assertThat(stats.getUnsafeCounter()).isZero();
        assertThat(stats.getLostUpdates()).isZero();
    }

    @Test
    @DisplayName("demonstrateRaceCondition with 50 threads should keep atomic and synchronized accurate while unsafe loses increments")
    void demonstrateRaceConditionShouldShowDifference() {
        int threads = 50;
        int incrementsPerThread = 100;
        long expectedTotal = (long) threads * incrementsPerThread; // 5000

        RaceConditionDemoResultDto result = counterService.demonstrateRaceCondition(threads, incrementsPerThread);

        assertThat(result.getThreadCount()).isEqualTo(threads);
        assertThat(result.getIncrementsPerThread()).isEqualTo(incrementsPerThread);
        assertThat(result.getExpectedTotal()).isEqualTo(expectedTotal);
        assertThat(result.getAtomicCounterResult()).isEqualTo(expectedTotal);
        assertThat(result.getSynchronizedCounterResult()).isEqualTo(expectedTotal);
        assertThat(result.getUnsafeCounterResult()).isLessThanOrEqualTo(expectedTotal);
        assertThat(result.getExecutionTimeMs()).isGreaterThanOrEqualTo(0);
    }
}
