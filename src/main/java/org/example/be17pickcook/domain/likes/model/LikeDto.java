package org.example.be17pickcook.domain.likes.model;

import lombok.*;
import org.example.be17pickcook.domain.user.model.User;

public class LikeDto {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private LikeTargetType targetType;
        private Long targetId;

        public Like toEntity(User user){
            return Like.builder()
                    .targetType(targetType)
                    .targetId(targetId)
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
        private int likeCount;
        private boolean hasLiked;
    }
}
