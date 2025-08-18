package org.example.be17pickcook.domain.review.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.recipe.model.Recipe;
import org.example.be17pickcook.domain.user.model.User;

import java.util.Date;
import java.util.List;

public class ReviewDto {

    @Getter
    @Builder
    @Schema(description = "리뷰 등록 요청 DTO")
    public static class ReviewRequestDto {
        @Schema(description = "리뷰 내용", example = "찌는 법도 쉽고 맛도 좋아서 이번여름 벌써 두번 시켜먹었네요 또 살 것 같아요!")
        private String content;
        @Schema(description = "평점 (1~5)", example = "5")
        private Integer rating;
        private List<String> image_urls;
        @Schema(description = "리뷰 대상 상품 ID", example = "101")
        private Long product_id;

        // DTO → Entity 변환 메서드
        public Review toEntity(User authUser, Product product) {
            return Review.builder()
                    .content(this.content)
                    .rating(this.rating)
                    .user(authUser)   // 작성자
                    .product(product)
                    .build();
        }

    }

    @Getter
    @Builder
    @Schema(description = "리뷰 응답 DTO")
    public static class ReviewResponseDto {
        @Schema(description = "리뷰 ID", example = "1")
        private Long id;
        @Schema(description = "리뷰 내용", example = "찌는 법도 쉽고 맛도 좋아서 이번여름 벌써 두번 시켜먹었네요 또 살 것 같아요!")
        private String content;
        @Schema(description = "평점 (1~5)", example = "5")
        private Integer rating;
        @Schema(description = "생성일")
        private Date createdAt;
        @Schema(description = "수정일")
        private Date updatedAt;

        public static ReviewResponseDto from(Review review) {
            return ReviewResponseDto.builder()
                    .id(review.getId())
                    .content(review.getContent())
                    .rating(review.getRating())
                    .createdAt(review.getCreatedAt())
                    .updatedAt(review.getUpdatedAt())
                    .build();
        }
        
    }
}
