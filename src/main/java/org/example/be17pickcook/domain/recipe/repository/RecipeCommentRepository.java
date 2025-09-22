package org.example.be17pickcook.domain.recipe.repository;

import org.example.be17pickcook.domain.recipe.model.RecipeComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeCommentRepository extends JpaRepository<RecipeComment, Long> {
    List<RecipeComment> findByRecipeIdxAndParentCommentIsNullOrderByCreatedAtAsc(Long recipeId);

    @Query("""
        SELECT rc.recipe.idx, COUNT(rc)
        FROM RecipeComment rc
        WHERE rc.recipe.idx IN :recipeIds
        GROUP BY rc.recipe.idx
    """)
    List<Object[]> countCommentsByRecipeIds(@Param("recipeIds") List<Long> recipeIds);
}
