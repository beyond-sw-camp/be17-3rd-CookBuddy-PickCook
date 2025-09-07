package org.example.be17pickcook.domain.community.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.be17pickcook.domain.user.model.User;

import java.util.List;

@Schema(description = "커뮤니티 댓글 관련 DTO 클래스들")
public class CommentDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "댓글 작성 요청 정보")
    public static class Request {

        @Schema(description = "댓글 내용", example = "정말 맛있어 보이네요! 레시피 감사합니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        private String content;

        @Schema(description = "게시글 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long postId;

        @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "5")
        private Long parentCommentId; // null이면 일반 댓글, 있으면 대댓글

        public Comment toEntity(User user, Post post, Comment parentComment){
            return Comment.builder()
                    .content(content)
                    .user(user)
                    .post(post)
                    .parentComment(parentComment)
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "댓글 조회 응답 정보")
    public static class Response {

        @Schema(description = "댓글 ID", example = "1")
        private Long id;

        @Schema(description = "게시글 ID", example = "1")
        private Long postId;

        @Schema(description = "댓글 내용", example = "정말 맛있어 보이네요! 레시피 감사합니다.")
        private String content;

        @Schema(description = "작성자 닉네임", example = "요리초보")
        private String userName;

        @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "5")
        private Long parentCommentId;

        @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
        private boolean hasLiked;

        @Schema(description = "댓글 좋아요 수", example = "3")
        private Long likeCount;

        @Schema(description = "대댓글 목록")
        private List<Response> children;

        // Entity → DTO 변환
        public static Response fromEntity(Comment comment,
                                          boolean hasLiked,
                                          List<Response> children) {
            return Response.builder()
                    .id(comment.getId())
                    .postId(comment.getPost().getId())
                    .content(comment.getContent())
                    .userName(comment.getUser().getNickname())
                    .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                    .hasLiked(hasLiked)
                    .likeCount(comment.getLikeCount())
                    .children(children)
                    .build();
        }
    }
}