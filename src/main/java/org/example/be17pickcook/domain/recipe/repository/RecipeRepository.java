package org.example.be17pickcook.domain.recipe.repository;

import org.example.be17pickcook.domain.community.model.Post;
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
    // 특정 게시글 ID와 작성자 ID로 게시글 조회
    Optional<Recipe> findByIdxAndUserIdx(Long recipeId, Integer userIdx);

    @Query("SELECT r FROM Recipe r " +
            "LEFT JOIN FETCH r.ingredients i " +
            "LEFT JOIN FETCH r.steps s " +
            "LEFT JOIN FETCH r.user u " +
            "LEFT JOIN FETCH r.nutrition n " +
            "WHERE r.idx = :id")
    Optional<Recipe> findDetailById(@Param("id") Long id);

    @Query("SELECT r.idx, r.title, r.cooking_method, r.category, r.time_taken, " +
            "r.difficulty_level, r.serving_size, r.hashtags, r.image_large_url, " +
            "r.likeCount, r.scrapCount, r.description FROM Recipe r")
    Page<Object[]> findAllOnlyRecipe(Pageable pageable);

    @Query("SELECT new org.example.be17pickcook.domain.recipe.model.RecipeListResponseDto(" +
            "r.idx, r.title, r.description, r.cooking_method, r.category, r.time_taken, " +
            "r.difficulty_level, r.serving_size, r.hashtags, r.image_large_url, " +
            "CAST(r.likeCount AS long), CAST(r.scrapCount AS long), CAST(r.scrapCount AS long), r.updatedAt) " +
            "FROM Recipe r " +
            "WHERE r.idx IN :ids")
    List<RecipeListResponseDto> findAllOnlyRecipeWithIds(@Param("ids") List<Long> ids);

    // 기존 findByFilters 메서드를 아래로 교체 (NULL과 빈 문자열 모두 처리)
    @Query("SELECT r FROM Recipe r WHERE " +
            "(:difficulty IS NULL OR :difficulty = '' OR r.difficulty_level = :difficulty) AND " +
            "(:category IS NULL OR :category = '' OR r.category = :category) AND " +
            "(:cookingMethod IS NULL OR :cookingMethod = '' OR r.cooking_method = :cookingMethod)")
    Page<Recipe> findByFilters(@Param("difficulty") String difficulty,
                               @Param("category") String category,
                               @Param("cookingMethod") String cookingMethod,
                               Pageable pageable);


    // 내가 작성한 레시피
    @Query("""
    SELECT new org.example.be17pickcook.domain.recipe.model.RecipeListResponseDto(
        r.idx,
        r.title,
        r.description,
        r.cooking_method,
        r.category,
        r.time_taken,
        r.difficulty_level,
        r.serving_size,
        r.hashtags,
        r.image_large_url, 
        r.likeCount,
        r.scrapCount,
        r.commentCount,
        r.updatedAt
    )
    FROM Recipe r
    WHERE r.user.idx = :userId
""")
    Page<RecipeListResponseDto> findAllByAuthorId(@Param("userId") Integer userId, Pageable pageable);


    // 내가 좋아요 누른 레시피
    @Query("""
    SELECT new org.example.be17pickcook.domain.recipe.model.RecipeListResponseDto(
        r.idx,
        r.title,
        r.description,
        r.cooking_method,
        r.category,
        r.time_taken,
        r.difficulty_level,
        r.serving_size,
        r.hashtags,
        r.image_large_url,
        r.likeCount,
        r.scrapCount,
        r.commentCount,
        r.updatedAt
    )
    FROM Recipe r
    JOIN Like l ON l.targetId = r.idx AND l.targetType = 'RECIPE'
    WHERE l.user.idx = :userId
""")
    Page<RecipeListResponseDto> findLikedRecipesByUser(@Param("userId") Integer userId, Pageable pageable);


    // 내가 스크랩한 레시피
    @Query("""
    SELECT new org.example.be17pickcook.domain.recipe.model.RecipeListResponseDto(
        r.idx,
        r.title,
        r.description,
        r.cooking_method,
        r.category,
        r.time_taken,
        r.difficulty_level,
        r.serving_size,
        r.hashtags,
        r.image_large_url,
        r.likeCount,
        r.scrapCount,
        r.commentCount,
        r.updatedAt
    )
    FROM Recipe r
    JOIN Scrap s ON s.targetId = r.idx AND s.targetType = 'RECIPE'
    WHERE s.user.idx = :userId
""")
    Page<RecipeListResponseDto> findScrappedRecipesByUser(@Param("userId") Integer userId, Pageable pageable);


    // 내가 댓글 단 레시피
    @Query("""
    SELECT new org.example.be17pickcook.domain.recipe.model.RecipeListResponseDto(
        r.idx,
        r.title,
        r.description,
        r.cooking_method,
        r.category,
        r.time_taken,
        r.difficulty_level,
        r.serving_size,
        r.hashtags,
        r.image_large_url,
        r.likeCount,
        r.scrapCount,
        r.commentCount,
        r.updatedAt
    )
    FROM Recipe r
    JOIN RecipeComment c ON c.recipe = r
    WHERE c.user.idx = :userId
    GROUP BY r.idx, r.title, r.cooking_method, r.category, r.time_taken,
             r.difficulty_level, r.serving_size, r.hashtags,
             r.image_large_url, r.likeCount, r.scrapCount
""")
    Page<RecipeListResponseDto> findRepliedRecipesByUser(@Param("userId") Integer userId, Pageable pageable);

}



