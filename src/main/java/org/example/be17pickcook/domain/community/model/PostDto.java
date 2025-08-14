package org.example.be17pickcook.domain.community.model;

import lombok.*;
import org.example.be17pickcook.domain.user.model.User;

import java.text.SimpleDateFormat;
import java.util.List;

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
        private String createdAt;

        public static Response toResponse(Post post) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            return Response.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .authorName(post.getUser().getNickname())
                    .createdAt(formatter.format(post.getCreatedAt()))
                    .build();
        }
    }

    @Getter
    @Builder
    public static class PostList {
        private List<Response> posts;

        public static PostList from(List<Post> entityList) {
            return PostList.builder()
                    .posts(entityList.stream().map(Response::toResponse).toList())
                    .build();
        }
    }
}
