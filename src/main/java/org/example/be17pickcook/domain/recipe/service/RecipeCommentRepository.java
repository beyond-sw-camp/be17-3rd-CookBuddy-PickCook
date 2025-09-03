package org.example.be17pickcook.domain.recipe.service;

import org.example.be17pickcook.domain.recipe.model.RecipeComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeCommentRepository extends JpaRepository<RecipeComment, Long> {
}
