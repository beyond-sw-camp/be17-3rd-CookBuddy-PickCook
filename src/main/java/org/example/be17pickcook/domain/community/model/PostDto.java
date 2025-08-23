package org.example.be17pickcook.domain.community.model;

import lombok.*;
import org.example.be17pickcook.domain.user.model.User;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    @Builder
    @Getter
    public static class ListResponse {
        private Long id;
        private String title;
        private String authorName;
        private String contentPreview; // 내용 요약 (최대 2줄)
        private String createdAgo;     // "n시간 전", "n일 전"
        private int likes;
        private int scraps;
        private int comments;

        public static ListResponse from(Post post, int likes, int scraps, int comments) {
            return ListResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .authorName(post.getUser().getNickname())
                    .contentPreview(generatePreview(post.getContent()))
                    .createdAgo(generateCreatedAgo(post.getCreatedAt()))
                    .likes(likes)
                    .scraps(scraps)
                    .comments(comments)
                    .build();
        }

        private static String generatePreview(String content) {
            if (content == null) return "";

            // 1. 이미지 태그 제거
            String noImg = content.replaceAll("<img[^>]*>", "");

            // 2. HTML 태그 제거 (굵게, 링크 등도 다 빼고 텍스트만 남김)
            String plainText = noImg.replaceAll("<[^>]*>", "");

            // 3. 줄바꿈 -> 공백으로 치환
            String noLineBreak = plainText.replaceAll("\\s+", " ").trim();

            // 4. 최대 길이 제한 (예: 100자)
            return noLineBreak.length() > 100
                    ? noLineBreak.substring(0, 100) + "..."
                    : noLineBreak;
        }

        private static String generateCreatedAgo(LocalDateTime createdAt) {
            Duration duration = Duration.between(createdAt, LocalDateTime.now());

            if (duration.toMinutes() < 60) {
                return duration.toMinutes() + "분 전";
            } else if (duration.toHours() < 24) {
                return duration.toHours() + "시간 전";
            } else if (duration.toDays() < 7) {
                return duration.toDays() + "일 전";
            } else {
                return createdAt.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"));
            }
        }
    }

    @Builder
    @Getter
    public static class DetailResponse {
        private Long id;
        private String title;
        private String content;
        private String authorName;
        private Integer likes;
        private boolean hasLiked;
        private Integer scraps;
        private boolean hasScrapped;
        private String updatedAt;

        public static DetailResponse from(Post post, int likes, boolean hasLiked, int scraps, boolean hasScrapped) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일");
            return DetailResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .authorName(post.getUser().getNickname())
                    .likes(likes)
                    .hasLiked(hasLiked)
                    .scraps(scraps)
                    .hasScrapped(hasScrapped)
                    .updatedAt(post.getUpdatedAt().format(formatter))
                    .build();
        }
    }
}
