package org.example.be17pickcook.domain.recipe.model;

import lombok.*;
import org.example.be17pickcook.domain.user.model.User;

public class RecipeCommentDto {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String content;
        private Long recipeId;
        private Long parentRecipeReviewId;

        public RecipeComment toEntity(User user, Recipe recipe, RecipeComment parentComment) {
            return RecipeComment.builder()
                    .content(content)
                    .user(user)
                    .recipe(recipe)
                    .parentComment(parentComment)
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long id;
        private Long recipeId;
        private Long parentRecipeReviewId;
    }

}
