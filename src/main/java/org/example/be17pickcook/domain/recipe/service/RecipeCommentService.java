package org.example.be17pickcook.domain.recipe.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.recipe.model.Recipe;
import org.example.be17pickcook.domain.recipe.model.RecipeComment;
import org.example.be17pickcook.domain.recipe.repository.RecipeRepository;
import org.example.be17pickcook.domain.user.model.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeCommentService {
    private final RecipeRepository recipeRepository;
    private final RecipeCommentRepository recipeCommentRepository;

    @Transactional
    public RecipeComment addReview(Long recipeId, String content, RecipeComment parentComment, User writer) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("레시피를 찾을 수 없음"));

        RecipeComment review = RecipeComment.builder()
                .content(content)
                .content(content)
                .recipe(recipe)
                .user(writer)
                .parentComment(parentComment)
                .build();

        recipeCommentRepository.save(review);
        recipe.increaseReviewCount(); // ✅ 리뷰 개수 증가

        return review;
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        RecipeComment review = recipeCommentRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없음"));

        Recipe recipe = review.getRecipe();
        recipeCommentRepository.delete(review);

        recipe.decreaseReviewCount(); // ✅ 리뷰 개수 감소
    }
}
