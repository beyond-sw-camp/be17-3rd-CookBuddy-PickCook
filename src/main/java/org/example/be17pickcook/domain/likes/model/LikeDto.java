// LikeDto.java
package org.example.be17pickcook.domain.likes.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.be17pickcook.domain.user.model.User;

@Schema(description = "좋아요 관련 DTO 클래스들")
public class LikeDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "좋아요 요청 정보")
    public static class Request {

        @Schema(description = "좋아요 대상 타입", example = "POST", allowableValues = {"POST", "COMMENT", "RECIPE"}, requiredMode = Schema.RequiredMode.REQUIRED)
        private LikeTargetType targetType;

        @Schema(description = "좋아요 대상 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
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
    @Schema(description = "좋아요 응답 정보")
    public static class Response {

        @Schema(description = "총 좋아요 수", example = "15")
        private int likeCount;

        @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
        private boolean hasLiked;
    }
}