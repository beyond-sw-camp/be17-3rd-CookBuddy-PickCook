package org.example.be17pickcook.domain.review.model;

import jakarta.validation.constraints.*;
import lombok.*;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.user.model.User;

import java.time.LocalDateTime;

public class ReviewDto {

    // ================== 등록 요청 DTO ==================
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewRequestDto {
        @NotNull(message = "productId는 필수입니다.")
        private Long productId;

        @NotBlank(message = "title은 필수입니다.")
        @Size(max = 100, message = "title은 100자 이하여야 합니다.")
        private String title;

        @NotBlank(message = "content는 필수입니다.")
        @Size(max = 2000, message = "content는 2000자 이하여야 합니다.")
        private String content;

        @NotNull(message = "rating은 필수입니다.")
        @Min(value = 1, message = "rating은 1 이상이어야 합니다.")
        @Max(value = 5, message = "rating은 5 이하여야 합니다.")
        private Integer rating;

        /** 요청 DTO → 엔티티 변환 (필수 파라미터 검증) */
        public Review toEntity(User user, Product product) {
            if (user == null || user.getIdx() == null) {
                throw new IllegalArgumentException("유효하지 않은 사용자입니다.");
            }
            if (product == null || product.getId() == null) {
                throw new IllegalArgumentException("유효하지 않은 상품입니다.");
            }
            if (rating == null) {
                throw new IllegalArgumentException("rating이 없습니다.");
            }
            return Review.builder()
                    .title(this.title)
                    .content(this.content)
                    .rating(this.rating)
                    .status(Review.Status.VISIBLE) // 기본값
                    .user(user)
                    .product(product)
                    .build();
        }
    }

    // ================== 응답 DTO ==================
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewResponseDto {
        private Long id;
        private String title;
        private String content;
        private Integer rating;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String userName;     // 작성자 이름
        private String productName;  // 상품명

        public static ReviewResponseDto fromEntity(Review review) {
            if (review == null) return null;

            String userNameSafe = null;
            if (review.getUser() != null) {
                // User 엔티티 필드명에 맞게 조정 (예: getName, getNickname 등)
                userNameSafe = review.getUser().getName();
            }

            String productNameSafe = null;
            if (review.getProduct() != null) {
                // Product 엔티티의 표시용 필드명에 맞게 조정 (예: getTitle/getName)
                productNameSafe = review.getProduct().getTitle();
            }

            return ReviewResponseDto.builder()
                    .id(review.getId())
                    .title(review.getTitle())
                    .content(review.getContent())
                    .rating(review.getRating())
                    .status(review.getStatus() != null ? review.getStatus().name() : null)
                    .createdAt(review.getCreatedAt())
                    .updatedAt(review.getUpdatedAt())
                    .userName(userNameSafe)
                    .productName(productNameSafe)
                    .build();
        }
    }
}
