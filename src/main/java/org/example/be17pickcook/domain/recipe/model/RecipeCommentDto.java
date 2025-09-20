package org.example.be17pickcook.domain.recipe.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.be17pickcook.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "레시피 댓글 관련 DTO 클래스들")
public class RecipeCommentDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "레시피 댓글 작성 요청 정보")
    public static class Request {

        @Schema(description = "레시피 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long recipeId;

        @Schema(description = "댓글 내용", example = "이 레시피로 만들어봤는데 정말 맛있어요!", requiredMode = Schema.RequiredMode.REQUIRED)
        private String content;

        @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "3")
        private Long parentCommentId; // 대댓글이면 부모 comment ID

        @Schema(description = "첨부 이미지 URL", example = "https://s3.amazonaws.com/bucket/key.png")
        private String imageUrl;

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
    @Schema(description = "레시피 댓글 조회 응답 정보")
    public static class Response {

        @Schema(description = "댓글 ID", example = "1")
        private Long id;

        @Schema(description = "댓글 내용", example = "이 레시피로 만들어봤는데 정말 맛있어요!")
        private String content;

        @Schema(description = "작성자 ID", example = "5")
        private int userId;

        @Schema(description = "작성자 닉네임", example = "요리러버")
        private String username;

        @Schema(description = "작성일시", example = "2025-01-15T14:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "수정일시", example = "2025-01-15T15:45:00")
        private LocalDateTime updatedAt;

        @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "3")
        private Long parentCommentId;

        @Schema(description = "첨부 이미지 URL", example = "https://example.com/comment-image.jpg")
        private String imageUrl; // 이미지 URL 하나만

        @Schema(description = "대댓글 목록")
        private List<Response> children;

        @Schema(description = "좋아요 수", example = "3")
        private Long likeCount;

        @Schema(description = "사용자가 좋아요를 눌렀는지 여부")
        private boolean hasLiked;

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
                    .likeCount(comment.getLikeCount())
                    .build(); // hasLiked는 서비스에서 따로 set
        }

        public void setHasLiked(boolean hasLiked) {
            this.hasLiked = hasLiked;
        }
    }
}