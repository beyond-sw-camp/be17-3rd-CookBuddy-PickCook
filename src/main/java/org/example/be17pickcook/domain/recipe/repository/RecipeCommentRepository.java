package org.example.be17pickcook.domain.recipe.repository;

import org.example.be17pickcook.domain.recipe.model.RecipeComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeCommentRepository extends JpaRepository<RecipeComment, Long> {
    List<RecipeComment> findByRecipeIdxAndParentCommentIsNullOrderByCreatedAtAsc(Long recipeId);
}
