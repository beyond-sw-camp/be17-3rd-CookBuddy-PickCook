// ScrapDto.java
package org.example.be17pickcook.domain.scrap.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.be17pickcook.domain.user.model.User;

@Schema(description = "스크랩 관련 DTO 클래스들")
public class ScrapDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "스크랩 요청 정보")
    public static class Request {

        @Schema(description = "스크랩 대상 타입", example = "POST", allowableValues = {"POST", "RECIPE"}, requiredMode = Schema.RequiredMode.REQUIRED)
        private ScrapTargetType targetType;

        @Schema(description = "스크랩 대상 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long targetId;

        public Scrap toEntity(User user){
            return Scrap.builder()
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
    @Schema(description = "스크랩 응답 정보")
    public static class Response {

        @Schema(description = "총 스크랩 수", example = "8")
        private int scrapCount;

        @Schema(description = "현재 사용자의 스크랩 여부", example = "false")
        private boolean hasScrapped;
    }
}