package org.example.be17pickcook.domain.scrap.model;

import lombok.*;
import org.example.be17pickcook.domain.user.model.User;

public class ScrapDto {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private ScrapTargetType targetType;
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
    public static class Response {
        private int scrapCount;
        private boolean hasScrapped;
    }
}
