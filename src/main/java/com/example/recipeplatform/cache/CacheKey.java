package com.example.recipeplatform.cache;

public record CacheKey(String queryType,
                       String authorUsername,
                       String categoryName,
                       int pageNumber,
                       int pageSize,
                       String sortString) {

    public static CacheKey from(String queryType,
                                String authorUsername,
                                String categoryName,
                                org.springframework.data.domain.Pageable pageable) {
        String sortString = pageable.getSort().toString();
        String normalizedAuthorUsername = authorUsername == null ? "" : authorUsername.trim().toLowerCase();
        String normalizedCategoryName = categoryName == null ? "" : categoryName.trim().toLowerCase();
        return new CacheKey(queryType, normalizedAuthorUsername, normalizedCategoryName,
                pageable.getPageNumber(), pageable.getPageSize(), sortString);
    }
}
