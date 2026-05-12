package com.example.recipeplatform.cache;

public record CacheKey(String queryType,
                       String authorUsername,
                       int pageNumber,
                       int pageSize,
                       String sortString) {

    public static CacheKey from(String queryType,
                                String authorUsername,
                                org.springframework.data.domain.Pageable pageable) {
        String sortString = pageable.getSort().toString();
        String normalizedAuthorUsername = authorUsername == null ? "" : authorUsername.trim().toLowerCase();
        return new CacheKey(queryType, normalizedAuthorUsername,
                pageable.getPageNumber(), pageable.getPageSize(), sortString);
    }
}
