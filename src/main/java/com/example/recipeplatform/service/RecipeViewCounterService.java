package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.CounterStatsDto;
import com.example.recipeplatform.dto.RaceConditionDemoResultDto;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RecipeViewCounterService {

    private int unsafeCounter = 0;

    private final AtomicLong atomicCounter = new AtomicLong(0);

    private int synchronizedCounter = 0;

    public void recordView() {
        incrementUnsafe();
        incrementAtomic();
        incrementSynchronized();
    }

    public void incrementUnsafe() {
        unsafeCounter++;
    }

    public void incrementAtomic() {
        atomicCounter.incrementAndGet();
    }

    public synchronized void incrementSynchronized() {
        synchronizedCounter++;
    }

    public synchronized void reset() {
        unsafeCounter = 0;
        atomicCounter.set(0);
        synchronizedCounter = 0;
    }

    public synchronized CounterStatsDto getStats() {
        long currentAtomic = atomicCounter.get();
        long currentSynchronized = synchronizedCounter;
        long currentUnsafe = unsafeCounter;

        long lostUpdates = Math.max(0, currentAtomic - currentUnsafe);

        CounterStatsDto stats = new CounterStatsDto();
        stats.setAtomicCounter(currentAtomic);
        stats.setSynchronizedCounter(currentSynchronized);
        stats.setUnsafeCounter(currentUnsafe);
        stats.setLostUpdates(lostUpdates);
        stats.setRaceConditionObserved(lostUpdates > 0);
        return stats;
    }

    public RaceConditionDemoResultDto demonstrateRaceCondition(int threadCount, int incrementsPerThread) {
        class LocalDemoCounters {
            int unsafe = 0;
            final AtomicLong atomic = new AtomicLong(0);
            int sync = 0;

            synchronized void incSync() {
                sync++;
            }
        }

        LocalDemoCounters demo = new LocalDemoCounters();
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        demo.unsafe++;
                        demo.atomic.incrementAndGet();
                        demo.incSync();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        try {
            finishLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        long executionTimeMs = System.currentTimeMillis() - startTime;
        long expectedTotal = (long) threadCount * incrementsPerThread;
        long lost = expectedTotal - demo.unsafe;

        RaceConditionDemoResultDto result = new RaceConditionDemoResultDto();
        result.setThreadCount(threadCount);
        result.setIncrementsPerThread(incrementsPerThread);
        result.setExpectedTotal(expectedTotal);
        result.setAtomicCounterResult(demo.atomic.get());
        result.setSynchronizedCounterResult(demo.sync);
        result.setUnsafeCounterResult(demo.unsafe);
        result.setLostUpdates(lost);
        result.setExecutionTimeMs(executionTimeMs);
        return result;
    }
}
