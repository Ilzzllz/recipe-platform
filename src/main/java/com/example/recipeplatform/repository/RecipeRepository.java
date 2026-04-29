package com.example.recipeplatform.repository;

import com.example.recipeplatform.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

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
}
