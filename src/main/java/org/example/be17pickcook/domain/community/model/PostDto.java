package org.example.be17pickcook.domain.community.model;

import jakarta.persistence.Column;
import lombok.*;
import org.example.be17pickcook.domain.user.model.User;
import org.hibernate.annotations.ColumnDefault;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        private Long likeCount;
        private Long scrapCount;
        private Long viewCount;
        private List<PostImageRequest> imageList;

        public Post toEntity(User user) {
            Post post = Post.builder()
                    .title(title)
                    .content(content)
                    .likeCount(0L)
                    .scrapCount(0L)
                    .viewCount(0L)
                    .user(user)
                    .build();

            if (imageList != null) {
                for (PostDto.PostImageRequest image : imageList) {
                    PostImage imageEntity = image.toEntity(post);
                    post.addImageUrl(imageEntity); // 이미지 하나씩 추가
                }
            }

            return post;
        }
    }


    @Getter
    @Builder
    public static class PostImageRequest {
        private String imageUrl;

        public PostImage toEntity(Post post) {
            return PostImage.builder()
                    .imageUrl(imageUrl)
                    .post(post)
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
        private Long likeCount;
        private Long scrapCount;
        private int comments;

        public static ListResponse from(Post post, int comments) {
            return ListResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .authorName(post.getUser().getNickname())
                    .contentPreview(generatePreview(post.getContent()))
                    .createdAgo(generateCreatedAgo(post.getCreatedAt()))
                    .likeCount(post.getLikeCount() != null ? post.getLikeCount() : 0L)
                    .scrapCount(post.getScrapCount() != null ? post.getScrapCount() : 0L)
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
        private Long likeCount;
        private boolean hasLiked;
        private Long scrapCount;
        private boolean hasScrapped;
        private String updatedAt;

        public static DetailResponse from(Post post, boolean hasLiked, boolean hasScrapped) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일");
            return DetailResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .authorName(post.getUser().getNickname())
                    .likeCount(post.getLikeCount() != null ? post.getLikeCount() : 0L)
                    .hasLiked(hasLiked)
                    .scrapCount(post.getScrapCount() != null ? post.getScrapCount() : 0L)
                    .hasScrapped(hasScrapped)
                    .updatedAt(post.getUpdatedAt().format(formatter))
                    .build();
        }
    }


    @Getter
    @Builder
    public static class PostCardResponse {
        private Long id;
        private String title;
        private String postImage;
        private String authorName;
        private String authorProfileImage;
        private Long likeCount;
        private boolean hasLiked;
        private Long scrapCount;
        private boolean hasScrapped;
        private Long viewCount;

//        public static PostCardResponse fromEntity(Post post) {
//            return PostCardResponse.builder()
//                    .id(post.getId())
//                    .title(post.getTitle())
//                    .postImage(post.getPostImageList().getImage)
//                    .authorName()
//        }
    }
}
