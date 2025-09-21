package org.example.be17pickcook.domain.recipe.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RecipeListResponseDto {
    @Schema(description = "레시피 ID", example = "1")
    private Long idx;
    @Schema(description = "레시피 제목", example = "김치찌개")
    private String title;
    @Schema(description = "레시피 설명", example = "맛있는 김치찌개")
    private String description;
    @Schema(description = "조리 방법", example = "끓이기")
    private String cooking_method;
    @Schema(description = "레시피 카테고리", example = "한식")
    private String category;
    @Schema(description = "소요 시간", example = "20분")
    private String time_taken;
    @Schema(description = "난이도", example = "어려움/보통/쉬움")
    private String difficulty_level;
    @Schema(description = "인분/양", example = "2인분")
    private String serving_size;
    @Schema(description = "해시태그", example = "#매운 #한식")
    private String hashtags;
    @Schema(description = "큰 이미지 URL")
    private String image_large_url;
    @Schema(description = "좋아요 수", example = "12")
    private Long likeCount;
    @Schema(description = "스크랩 수", example = "12")
    private Long scrapCount;
    @Schema(description = "로그인 사용자가 좋아요를 눌렀는지 여부", example = "true")
    private Boolean likedByUser;
    @Schema(description = "로그인 사용자가 스크랩을 눌렀는지 여부", example = "true")
    private Boolean scrappedByUser;
    @Schema(description = "레시피 게시물에 달린 댓글 수", example = "34")
    private Long commentCount;
    @Schema(description = "레시피 업데이트일", example = "2025.08.15")
    private LocalDateTime updatedAt;


    @Builder
    public RecipeListResponseDto(Long idx, String title, String description, String cooking_method, String category,
                                 String time_taken, String difficulty_level, String serving_size,
                                 String hashtags, String image_large_url, Long likeCount, Long scrapCount,
                                 Long commentCount, Boolean likedByUser, Boolean scrappedByUser, LocalDateTime updatedAt) {
        this.idx = idx;
        this.title = title;
        this.description = description;
        this.cooking_method = cooking_method;
        this.category = category;
        this.time_taken = time_taken;
        this.difficulty_level = difficulty_level;
        this.serving_size = serving_size;
        this.hashtags = hashtags;
        this.image_large_url = image_large_url;
        this.likeCount = likeCount;
        this.scrapCount = scrapCount;
        this.commentCount = commentCount;
        this.likedByUser = likedByUser;
        this.scrappedByUser = scrappedByUser;
        this.updatedAt = updatedAt;
    }


    public RecipeListResponseDto(Long idx, String title, String description, String cooking_method, String category,
                                 String time_taken, String difficulty_level, String serving_size,
                                 String hashtags, String image_large_url, Long likeCount, Long scrapCount,
                                 Long commentCount, LocalDateTime updatedAt) {
        this.idx = idx;
        this.title = title;
        this.description = description;
        this.cooking_method = cooking_method;
        this.category = category;
        this.time_taken = time_taken;
        this.difficulty_level = difficulty_level;
        this.serving_size = serving_size;
        this.hashtags = hashtags;
        this.image_large_url = image_large_url;
        this.likeCount = likeCount;
        this.scrapCount = scrapCount;
        this.commentCount = commentCount;
        this.updatedAt = updatedAt;
    }

    // 좋아요 관련 값 세팅 메서드 (반 정규화 전)
//        public void setLikeInfo(Integer likeCount, Boolean likedByUser) {
//            this.likeCount = likeCount;
//            this.likedByUser = likedByUser;
//        }
    public void setLikedByUser(Boolean likedByUser) {
        this.likedByUser = likedByUser;
    }

    // 스크랩 관련 값 세팅 메서드
    public void setScrapInfo(Boolean scrappedByUser) {
        this.scrappedByUser = scrappedByUser;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }
}
