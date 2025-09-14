package org.example.be17pickcook.domain.recipe.repository;

import org.example.be17pickcook.domain.recipe.model.Recipe;
import org.example.be17pickcook.domain.recipe.model.RecipeDto;
import org.example.be17pickcook.domain.recipe.model.RecipeListResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    @Query("SELECT r FROM Recipe r " +
            "LEFT JOIN FETCH r.ingredients i " +
            "LEFT JOIN FETCH r.user u " +
            "LEFT JOIN FETCH r.nutrition n " + // ~~ToOne은 한 개이기 때문에 중복 안 생김
            "WHERE r.idx = :id")
    Optional<Recipe> findDetailById(@Param("id") Long id);

    @Query("SELECT r.idx, r.title, r.cooking_method, r.category, r.time_taken, " +
            "r.difficulty_level, r.serving_size, r.hashtags, r.image_large_url, r.likeCount, r.scrapCount, r.description FROM Recipe r")
    Page<Object[]> findAllOnlyRecipe(Pageable pageable);

    @Query("SELECT new org.example.be17pickcook.domain.recipe.model.RecipeListResponseDto(" +
            "r.idx, r.title, r.cooking_method, r.category, r.time_taken, " +
            "r.difficulty_level, r.serving_size, r.hashtags, r.image_large_url, " +
            "CAST(r.likeCount AS long), CAST(r.scrapCount AS long), false, false) " +
            "FROM Recipe r " +
            "WHERE r.idx IN :ids")
    List<RecipeListResponseDto> findAllOnlyRecipeWithIds(@Param("ids") List<Long> ids);

}



