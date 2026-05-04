package com.example.recipeplatform.repository;

import com.example.recipeplatform.model.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    boolean existsByTitleIgnoreCase(String title);

    @Query("""
            select distinct r from Recipe r
            left join fetch r.author
            left join fetch r.category
            left join fetch r.ingredients
            left join fetch r.steps
            order by r.id
            """)
    List<Recipe> findAllWithFetchJoin();

    @Query("""
            select distinct r from Recipe r
            left join fetch r.author
            left join fetch r.category
            left join fetch r.ingredients
            left join fetch r.steps
            where r.id = :id
            """)
    Optional<Recipe> findByIdWithFetchJoin(@Param("id") Long id);

    @Query("""
            select distinct r from Recipe r
            left join fetch r.author
            left join fetch r.category
            left join fetch r.ingredients
            left join fetch r.steps
            where lower(r.title) like lower(concat('%', :title, '%'))
            order by r.id
            """)
    List<Recipe> searchWithFetchJoin(@Param("title") String title);

    long countByTitleStartingWith(String prefix);

    @Query(value = """
            SELECT DISTINCT r FROM Recipe r
            JOIN FETCH r.author a
            JOIN FETCH r.category c
            LEFT JOIN FETCH r.ingredients i
            LEFT JOIN FETCH r.steps s
            WHERE LOWER(a.username) = LOWER(:authorUsername)
            """,
            countQuery = "SELECT COUNT(r) FROM Recipe r JOIN r.author a WHERE LOWER(a.username) = LOWER(:authorUsername)")
    Page<Recipe> findByAuthorUsernameJPQL(@Param("authorUsername") String authorUsername, Pageable pageable);

    @Query(value = """
            SELECT r.* FROM recipes r
            JOIN users u ON r.author_id = u.id
            WHERE LOWER(u.username) = LOWER(:authorUsername)
            ORDER BY r.id
            LIMIT :limit OFFSET :offset
            """,
            countQuery = """
            SELECT COUNT(*) FROM recipes r
            JOIN users u ON r.author_id = u.id
            WHERE LOWER(u.username) = LOWER(:authorUsername)
            """,
            nativeQuery = true)
    Page<Recipe> findByAuthorUsernameNative(@Param("authorUsername") String authorUsername,
                                            Pageable pageable);
}