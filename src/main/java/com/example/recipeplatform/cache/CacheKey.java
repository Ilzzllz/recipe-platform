package com.example.recipeplatform.cache;

import org.springframework.data.domain.Sort;

import java.util.Objects;

public record CacheKey(String authorUsername,
                       int pageNumber,
                       int pageSize,
                       String sortString) {

    public static CacheKey from(String authorUsername, org.springframework.data.domain.Pageable pageable) {
        String sortString = pageable.getSort().toString();
        return new CacheKey(authorUsername, pageable.getPageNumber(), pageable.getPageSize(), sortString);
    }
}