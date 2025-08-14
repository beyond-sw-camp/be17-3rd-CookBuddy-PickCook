package org.example.be17pickcook.domain.community.model;

import lombok.*;
import org.example.be17pickcook.domain.user.model.User;

public class CommentDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String content;
        private Long postId;

        // DTO → Entity 변환
        public Comment toEntity(Post post, User user) {
            return Comment.builder()
                    .content(this.content)
                    .post(post)
                    .user(user)
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

        // Entity → DTO 변환
        public static Response fromEntity(Comment comment) {
            return Response.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .userName(comment.getUser().getNickname())
                    .build();
        }
    }
}
