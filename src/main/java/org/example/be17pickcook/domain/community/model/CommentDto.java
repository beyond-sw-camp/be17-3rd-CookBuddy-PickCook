package org.example.be17pickcook.domain.community.model;

import lombok.*;
import org.example.be17pickcook.domain.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class CommentDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String content;
        private Long postId;
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
    public static class Response {
        private Long id;
        private String content;
        private String userName;
        private Long parentCommentId;           // 부모 댓글 ID
        private List<Response> children;        // 대댓글 리스트

        // Entity → DTO 변환
        public static Response fromEntity(Comment comment) {
            return Response.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .userName(comment.getUser().getNickname())
                    .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                    .children(comment.getChildren().stream()
                            .map(Response::fromEntity)
                            .collect(Collectors.toList()))
                    .build();
        }
    }
}
