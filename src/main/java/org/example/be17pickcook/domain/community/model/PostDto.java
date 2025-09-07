package org.example.be17pickcook.domain.community.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.*;
import org.example.be17pickcook.domain.user.model.User;
import org.hibernate.annotations.ColumnDefault;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Schema(description = "커뮤니티 게시글 관련 DTO 클래스들")
public class PostDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "게시글 작성 요청 정보")
    public static class Request {

        @Schema(description = "게시글 제목", example = "맛있는 김치찌개 레시피 공유해요", requiredMode = Schema.RequiredMode.REQUIRED)
        private String title;

        @Schema(description = "게시글 내용", example = "오늘 만든 김치찌개가 정말 맛있어서 레시피를 공유합니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        private String content;

        @Schema(description = "좋아요 수 (초기값 0)", example = "0")
        private Long likeCount;

        @Schema(description = "스크랩 수 (초기값 0)", example = "0")
        private Long scrapCount;

        @Schema(description = "조회 수 (초기값 0)", example = "0")
        private Long viewCount;

        @Schema(description = "첨부 이미지 목록")
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
                    post.addImageUrl(imageEntity);
                }
            }

            return post;
        }
    }

    @Getter
    @Builder
    @Schema(description = "게시글 이미지 요청 정보")
    public static class PostImageRequest {

        @Schema(description = "이미지 URL", example = "https://example.com/image.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
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
    @Schema(description = "게시글 목록 조회 응답 정보")
    public static class ListResponse {

        @Schema(description = "게시글 ID", example = "1")
        private Long id;

        @Schema(description = "게시글 제목", example = "맛있는 김치찌개 레시피 공유해요")
        private String title;

        @Schema(description = "작성자 닉네임", example = "요리왕")
        private String authorName;

        @Schema(description = "내용 미리보기 (최대 100자)", example = "오늘 만든 김치찌개가 정말 맛있어서...")
        private String contentPreview;

        @Schema(description = "작성 시간 (상대시간)", example = "2시간 전")
        private String createdAgo;

        @Schema(description = "좋아요 수", example = "15")
        private Long likeCount;

        @Schema(description = "스크랩 수", example = "8")
        private Long scrapCount;

        @Schema(description = "댓글 수", example = "12")
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
    @Schema(description = "게시글 상세 조회 응답 정보")
    public static class DetailResponse {

        @Schema(description = "게시글 ID", example = "1")
        private Long id;

        @Schema(description = "게시글 제목", example = "맛있는 김치찌개 레시피 공유해요")
        private String title;

        @Schema(description = "게시글 내용", example = "오늘 만든 김치찌개가 정말 맛있어서 레시피를 공유합니다...")
        private String content;

        @Schema(description = "작성자 닉네임", example = "요리왕")
        private String authorName;

        @Schema(description = "좋아요 수", example = "15")
        private Long likeCount;

        @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
        private boolean hasLiked;

        @Schema(description = "스크랩 수", example = "8")
        private Long scrapCount;

        @Schema(description = "현재 사용자의 스크랩 여부", example = "false")
        private boolean hasScrapped;

        @Schema(description = "수정일시", example = "2025년 1월 15일")
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
    @Schema(description = "게시글 카드형 응답 정보 (메인 페이지용)")
    public static class PostCardResponse {

        @Schema(description = "게시글 ID", example = "1")
        private Long id;

        @Schema(description = "게시글 제목", example = "맛있는 김치찌개 레시피 공유해요")
        private String title;

        @Schema(description = "대표 이미지 URL", example = "https://example.com/image.jpg")
        private String postImage;

        @Schema(description = "작성자 닉네임", example = "요리왕")
        private String authorName;

        @Schema(description = "작성자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String authorProfileImage;

        @Schema(description = "좋아요 수", example = "15")
        private Long likeCount;

        @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
        private boolean hasLiked;

        @Schema(description = "스크랩 수", example = "8")
        private Long scrapCount;

        @Schema(description = "현재 사용자의 스크랩 여부", example = "false")
        private boolean hasScrapped;

        @Schema(description = "조회 수", example = "120")
        private Long viewCount;

        @Schema(description = "작성일(수정일)", example = "2025.08.25")
        private LocalDateTime updatedAt;

        @Schema(description = "게시글 내용", example = "내용 본문")
        private String content;

        @Schema(description = "댓글 수", example = "12")
        private Long commentCount;

        public void setHasLiked(boolean hasLiked) {
            this.hasLiked = hasLiked;
        }

        public void setHasScrapped(boolean hasScrapped) {
            this.hasScrapped = hasScrapped;
        }

        // Entity -> DTO 변환
        public static PostCardResponse fromEntity(Post post) {
            String firstImage = null;
            if (post.getPostImageList() != null && !post.getPostImageList().isEmpty()) {
                firstImage = post.getPostImageList().get(0).getImageUrl();
            }

            return PostCardResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .postImage(firstImage)
                    .authorName(post.getUser().getNickname())
                    .authorProfileImage(post.getUser().getProfileImage())
                    .likeCount(post.getLikeCount() != null ? post.getLikeCount() : 0L)
                    .scrapCount(post.getScrapCount() != null ? post.getScrapCount() : 0L)
                    .viewCount(post.getViewCount() != null ? post.getViewCount() : 0L)
                    .updatedAt(post.getUpdatedAt())
                    .content(post.getContent())
                    .build();
        }
    }
}