package com.example.recipeplatform.cache;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RecipeQueryCacheService {

    private final ConcurrentHashMap<CacheKey, Page<?>> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Page<T> get(CacheKey key) {
        return (Page<T>) cache.get(key);
    }

    public <T> void put(CacheKey key, Page<T> page) {
        cache.put(key, page);
    }

    public void invalidateAll() {
        cache.clear();
    }
}