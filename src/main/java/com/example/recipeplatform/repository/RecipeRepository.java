package com.example.recipeplatform.repository;

import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.repository.projection.RecipeFilterProjection;
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

    @Query("""
            select r.id as recipeId,
                   r.title as recipeTitle,
                   r.description as recipeDescription,
                   a.id as authorId,
                   a.username as authorUsername,
                   c.id as categoryId,
                   c.name as categoryName
            from Recipe r
            join r.author a
            join r.category c
            where lower(r.author.username) = lower(:authorUsername)
              and lower(r.category.name) = lower(:categoryName)
            """)
    Page<RecipeFilterProjection> findByAuthorUsernameJPQL(@Param("authorUsername") String authorUsername,
                                                          @Param("categoryName") String categoryName,
                                                          Pageable pageable);

    @Query(value = """
            select r.id as recipeId,
                   r.title as recipeTitle,
                   r.description as recipeDescription,
                   u.id as authorId,
                   u.username as authorUsername,
                   c.id as categoryId,
                   c.name as categoryName
            from recipes r
            join users u on r.author_id = u.id
            join categories c on r.category_id = c.id
            where lower(u.username) = lower(:authorUsername)
              and lower(c.name) = lower(:categoryName)
            """,
            countQuery = """
            select count(*)
            from recipes r
            join users u on r.author_id = u.id
            join categories c on r.category_id = c.id
            where lower(u.username) = lower(:authorUsername)
              and lower(c.name) = lower(:categoryName)
            """,
            nativeQuery = true)
    Page<RecipeFilterProjection> findByAuthorUsernameNative(@Param("authorUsername") String authorUsername,
                                                            @Param("categoryName") String categoryName,
                                                            Pageable pageable);
}
