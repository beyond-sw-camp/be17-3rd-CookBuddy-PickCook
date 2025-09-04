package org.example.be17pickcook.domain.recipe.model;

import lombok.*;
import org.example.be17pickcook.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeCommentDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private Long recipeId;
        private String content;
        private Long parentCommentId; // 대댓글이면 부모 comment ID

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
        private String content;
        private int userId;
        private String username;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long parentCommentId;
        private String imageUrl; // 이미지 URL 하나만
        private List<Response> children;

        public static Response fromEntity(RecipeComment comment) {
            return Response.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .userId(comment.getUser().getIdx())
                    .username(comment.getUser().getNickname())
                    .createdAt(comment.getCreatedAt())
                    .updatedAt(comment.getUpdatedAt())
                    .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                    .imageUrl(comment.getImage() != null ? comment.getImage().getImageUrl() : null)
                    .children(comment.getChildren() != null ?
                            comment.getChildren().stream()
                                    .map(Response::fromEntity)
                                    .collect(Collectors.toList())
                            : new ArrayList<>())
                    .build();
        }
    }
}

