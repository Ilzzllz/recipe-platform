package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.CounterStatsDto;
import com.example.recipeplatform.dto.RaceConditionDemoResultDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeViewCounterServiceTest {

    private RecipeViewCounterService counterService;

    @BeforeEach
    void setUp() {
        counterService = new RecipeViewCounterService();
    }

    @AfterEach
    void tearDown() {
        Thread.interrupted();
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
    @DisplayName("getStats should report a race condition when unsafe increments are lost")
    void getStatsShouldReportRaceConditionWhenUnsafeCounterLags() {
        counterService.incrementAtomic();

        CounterStatsDto stats = counterService.getStats();

        assertThat(stats.getLostUpdates()).isOne();
        assertThat(stats.isRaceConditionObserved()).isTrue();
    }

    @Test
    @DisplayName("demonstrateRaceCondition with 50 threads should keep atomic and synchronized accurate while unsafe loses increments")
    void demonstrateRaceConditionShouldShowDifference() {
        int threads = 50;
        int incrementsPerThread = 100;
        long expectedTotal = (long) threads * incrementsPerThread;

        RaceConditionDemoResultDto result = counterService.demonstrateRaceCondition(threads, incrementsPerThread);

        assertThat(result.getThreadCount()).isEqualTo(threads);
        assertThat(result.getIncrementsPerThread()).isEqualTo(incrementsPerThread);
        assertThat(result.getExpectedTotal()).isEqualTo(expectedTotal);
        assertThat(result.getAtomicCounterResult()).isEqualTo(expectedTotal);
        assertThat(result.getSynchronizedCounterResult()).isEqualTo(expectedTotal);
        assertThat(result.getUnsafeCounterResult()).isLessThanOrEqualTo(expectedTotal);
        assertThat(result.getExecutionTimeMs()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("handleInterruption should restore the interrupted flag on the current thread")
    void handleInterruptionShouldRestoreFlag() {
        Thread.interrupted();

        counterService.handleInterruption();

        assertThat(Thread.currentThread().isInterrupted()).isTrue();

        Thread.interrupted();
    }

    @Test
    @DisplayName("awaitQuietly should return false and restore flag when interrupted")
    void awaitQuietlyShouldHandleInterruption() {
        CountDownLatch latch = new CountDownLatch(1);
        Thread.currentThread().interrupt();

        boolean result = counterService.awaitQuietly(latch);

        assertThat(result).isFalse();
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        Thread.interrupted();
    }

    @Test
    @DisplayName("awaitWithTimeout should return false and restore flag when interrupted")
    void awaitWithTimeoutShouldHandleInterruption() {
        CountDownLatch latch = new CountDownLatch(1);
        Thread.currentThread().interrupt();

        boolean result = counterService.awaitWithTimeout(latch, 1, TimeUnit.SECONDS);

        assertThat(result).isFalse();
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        Thread.interrupted();
    }

    @Test
    @DisplayName("demonstrateRaceCondition should skip increments when a worker is interrupted before start")
    void demonstrateRaceConditionShouldHandleInterruptedWorker() {
        RecipeViewCounterService interruptedService = new RecipeViewCounterService() {
            @Override
            ExecutorService createExecutor(int threadCount) {
                return new InterruptingExecutorService();
            }
        };

        RaceConditionDemoResultDto result = interruptedService.demonstrateRaceCondition(1, 10);

        assertThat(result.getAtomicCounterResult()).isZero();
        assertThat(result.getSynchronizedCounterResult()).isZero();
        assertThat(result.getUnsafeCounterResult()).isZero();
        assertThat(result.getLostUpdates()).isEqualTo(10);
    }

    private static final class InterruptingExecutorService extends AbstractExecutorService {
        private boolean shutdown;

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public java.util.List<Runnable> shutdownNow() {
            shutdown = true;
            return Collections.emptyList();
        }

        @Override
        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return shutdown;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return shutdown;
        }

        @Override
        public void execute(Runnable command) {
            Thread worker = new Thread(() -> {
                Thread.currentThread().interrupt();
                command.run();
            });
            worker.start();
        }
    }
}
