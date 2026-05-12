package com.example.recipeplatform.cache;

import com.example.recipeplatform.dto.RecipeDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RecipeQueryCacheService {

    private final Map<CacheKey, Page<RecipeDto>> cache = new HashMap<>();

    public synchronized Page<RecipeDto> get(CacheKey key) {
        return cache.get(key);
    }

    public synchronized void put(CacheKey key, Page<RecipeDto> page) {
        cache.put(key, page);
    }

    public synchronized void invalidateAll() {
        cache.clear();
    }
}
