package org.example.be17pickcook.domain.community.model;

import lombok.*;
import org.example.be17pickcook.domain.user.model.User;

import java.text.SimpleDateFormat;

public class PostDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String title;
        private String content;

        public Post toEntity(User user) {
            return Post.builder()
                    .title(title)
                    .content(content)
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
        private String title;
        private String content;
        private String authorName;
        private Integer likes;
        private Integer scraps;

        public static Response toResponse(Post post, int likes, int scraps) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            return Response.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .authorName(post.getUser().getNickname())
                    .likes(likes)
                    .scraps(scraps)
                    .build();
        }
    }
}
