package org.example.be17pickcook.domain.review.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.be17pickcook.domain.order.model.Orders;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 리뷰 관련 DTO 클래스들
 * - Request: 리뷰 작성/수정 요청
 * - Response: 리뷰 조회 응답
 * - Filter: 검색/필터 조건
 */
@Schema(description = "리뷰 관련 DTO 클래스들")
public class ReviewDto {

    // =================================================================
    // 리뷰 작성 요청 DTO
    // =================================================================

    @Schema(description = "리뷰 작성 요청")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WriteRequest {

        @Schema(description = "리뷰 내용", example = "배송도 빠르고 상품 상태도 좋았습니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "리뷰 내용을 입력해주세요.")
        @Size(max = 2000, message = "리뷰 내용은 2000자 이하로 작성해주세요.")
        private String content;

        @Schema(description = "별점 (1~5점)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "별점을 선택해주세요.")
        @Min(value = 1, message = "별점은 1점부터 5점까지 선택할 수 있습니다.")
        @Max(value = 5, message = "별점은 1점부터 5점까지 선택할 수 있습니다.")
        private Integer rating;

        @Schema(description = "리뷰 이미지 목록 (최대 5개)")
        @Size(max = 5, message = "리뷰 이미지는 최대 5개까지 업로드할 수 있습니다.")
        private List<ReviewImageRequest> images;

        public Review toEntity(User authUser, Product product, Orders order) {
            Review review = Review.builder()
                    .content(content)
                    .rating(rating)
                    .product(product)
                    .order(order)
                    .user(authUser)
                    .build();

            if (images != null && !images.isEmpty()) {
                List<ReviewImage> reviewImages = images.stream()
                        .map(img -> ReviewImage.builder()
                                .imageUrl(img.getImageUrl())
                                .originalFilename(img.getOriginalFilename())
                                .contentType(img.getContentType())
                                .fileSize(img.getFileSize())
                                .imageOrder(img.getImageOrder())
                                .review(review) // FK 연결
                                .build())
                        .toList();

                review.getImages().addAll(reviewImages);
            }

            return review;
        }
    }


    @Schema(description = "리뷰 이미지 요청 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewImageRequest {

        @Schema(description = "이미지 URL", example = "https://s3.../image1.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "이미지 URL은 필수입니다.")
        private String imageUrl;

        @Schema(description = "원본 파일명", example = "product.jpg")
        private String originalFilename;

        @Schema(description = "MIME 타입", example = "image/jpeg")
        private String contentType;

        @Schema(description = "파일 크기 (bytes)", example = "123456")
        private Long fileSize;

        @Schema(description = "이미지 순서 (1~5)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer imageOrder;
    }


    // =================================================================
    // 리뷰 수정 요청 DTO
    // =================================================================

    @Schema(description = "리뷰 수정 요청")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {

        @Schema(description = "리뷰 내용", example = "수정된 리뷰 내용")
        @Size(max = 2000, message = "리뷰 내용은 2000자 이하로 작성해주세요.")
        private String content;

        @Schema(description = "별점 (1~5점)", example = "4")
        @Min(value = 1, message = "별점은 1점부터 5점까지 선택할 수 있습니다.")
        @Max(value = 5, message = "별점은 1점부터 5점까지 선택할 수 있습니다.")
        private Integer rating;

        @Schema(description = "이미지 URL 목록 (최대 5개)")
        @Size(max = 5, message = "리뷰 이미지는 최대 5개까지 업로드할 수 있습니다.")
        private List<String> imageUrls;
    }

    // =================================================================
    // 리뷰 응답 DTO
    // =================================================================

    @Schema(description = "리뷰 응답 정보")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        @Schema(description = "리뷰 ID", example = "1")
        private Long reviewId;

        @Schema(description = "상품 ID", example = "10")
        private Long productId;

        @Schema(description = "상품명", example = "맛있는 김치찌개")
        private String productName; // 추가

        @Schema(description = "리뷰 내용", example = "배송도 빠르고 상품 상태도 좋았습니다.")
        private String content;

        @Schema(description = "별점 (1~5점)", example = "5")
        private Integer rating;

        @Schema(description = "작성자 정보")
        private AuthorInfo author;

        @Schema(description = "이미지 목록")
        private List<ImageInfo> images;

        @Schema(description = "작성일", example = "2024.08.15")
        private String createdAt;

        @Schema(description = "수정일", example = "2024.08.16")
        private String updatedAt;

        @Schema(description = "내가 작성한 리뷰 여부", example = "true")
        private Boolean isMyReview;

        @Schema(description = "수정 가능 여부 (작성 후 7일 이내)", example = "true")
        private Boolean canModify;

        @Schema(description = "이미지 개수", example = "2")
        private Integer imageCount;

        public static Response fromEntity(Review review, Integer currentUserId) {
            return Response.builder()
                    .reviewId(review.getReviewId())
                    .productId(review.getProduct().getId())
                    .content(review.getContent())
                    .rating(review.getRating())
                    .author(AuthorInfo.fromUser(review.getUser()))
                    .images(review.getImages().stream()
                            .map(ImageInfo::fromEntity)
                            .toList())
                    .createdAt(formatDateTime(review.getCreatedAt()))
                    .updatedAt(formatDateTime(review.getUpdatedAt()))
                    .isMyReview(review.isWrittenBy(currentUserId))
                    .canModify(review.isModifiable() && review.isWrittenBy(currentUserId))
                    .imageCount(review.getImageCount())
                    .build();
        }

        // Entity -> Response 변환
        public static Response fromEntityWithUserContext(Review review, Integer currentUserId) {
            return Response.builder()
                    .reviewId(review.getReviewId())
                    .productId(review.getProduct().getId())
                    .productName(review.getProduct().getTitle()) // 상품명 추가
                    .content(review.getContent())
                    .rating(review.getRating())
                    .author(AuthorInfo.fromUser(review.getUser()))
                    .images(review.getImages().stream()
                            .sorted((a, b) -> Integer.compare(a.getImageOrder(), b.getImageOrder()))
                            .map(ImageInfo::fromEntity)
                            .toList())
                    .createdAt(formatDateTime(review.getCreatedAt()))
                    .updatedAt(formatDateTime(review.getUpdatedAt()))
                    .isMyReview(currentUserId != null && review.getUser().getIdx().equals(currentUserId))
                    .canModify(review.isModifiable() && review.isWrittenBy(currentUserId))
                    .imageCount(review.getImageCount())
                    .build();
        }

        private static String formatDateTime(LocalDateTime dateTime) {
            if (dateTime == null) return null;
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        }
    }





    // =================================================================
    // 작성자 정보 DTO
    // =================================================================

    @Schema(description = "리뷰 작성자 정보")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {

        @Schema(description = "작성자 ID", example = "12")
        private Integer userId;

        @Schema(description = "마스킹된 닉네임", example = "김**")
        private String maskedNickname;

        @Schema(description = "프로필 이미지 URL", example = "https://s3.../profile.jpg")
        private String profileImageUrl;

        public static AuthorInfo fromUser(org.example.be17pickcook.domain.user.model.User user) {
            return AuthorInfo.builder()
                    .userId(user.getIdx())
                    .maskedNickname(maskNickname(user.getNickname()))
                    .profileImageUrl(user.getProfileImage())
                    .build();
        }

        // 닉네임 마스킹: "김길동" -> "김**"
        private static String maskNickname(String nickname) {
            if (nickname == null || nickname.length() <= 1) {
                return nickname;
            }

            if (nickname.length() == 2) {
                return nickname.charAt(0) + "*";
            }

            return nickname.charAt(0) + "*".repeat(nickname.length() - 1);
        }
    }

    // =================================================================
    // 이미지 정보 DTO
    // =================================================================

    @Schema(description = "리뷰 이미지 정보")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {

        @Schema(description = "이미지 ID", example = "1")
        private Long imageId;

        @Schema(description = "이미지 URL", example = "https://s3.../review_image.jpg")
        private String imageUrl;

        @Schema(description = "썸네일 URL", example = "https://s3.../review_image_thumb.jpg")
        private String thumbnailUrl;

        @Schema(description = "이미지 순서", example = "1")
        private Integer imageOrder;

        @Schema(description = "원본 파일명", example = "photo.jpg")
        private String originalFilename;

        public static ImageInfo fromEntity(ReviewImage reviewImage) {
            return ImageInfo.builder()
                    .imageId(reviewImage.getImageId())
                    .imageUrl(reviewImage.getImageUrl())
                    .thumbnailUrl(reviewImage.getThumbnailUrl())
                    .imageOrder(reviewImage.getImageOrder())
                    .originalFilename(reviewImage.getOriginalFilename())
                    .build();
        }
    }

    // =================================================================
    // 리뷰 목록 응답 DTO
    // =================================================================

    @Schema(description = "리뷰 목록 응답 (페이징)")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {

        @Schema(description = "리뷰 목록")
        private List<Response> reviews;

        @Schema(description = "리뷰 통계 정보")
        private StatisticsResponse statistics;

        @Schema(description = "페이징 정보")
        private PageInfo pageInfo;
    }

    // =================================================================
    // 리뷰 통계 정보 DTO
    // =================================================================

    @Schema(description = "리뷰 통계 정보")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsResponse {

        @Schema(description = "총 리뷰 수", example = "150")
        private Long totalReviews;

        @Schema(description = "평균 별점", example = "4.2")
        private Double averageRating;

        @Schema(description = "별점별 개수 [1점, 2점, 3점, 4점, 5점]", example = "[5, 8, 15, 45, 77]")
        private Long[] ratingDistribution;

        @Schema(description = "이미지 있는 리뷰 수", example = "85")
        private Long reviewsWithImages;

        @Schema(description = "이미지 있는 리뷰 비율", example = "56.7")
        private Double imageReviewRatio;

        public static StatisticsResponse fromRepositoryResult(
                org.example.be17pickcook.domain.review.repository.ReviewRepositoryCustom.ReviewStatistics stats) {

            Double imageRatio = stats.getTotalReviews() > 0
                    ? (stats.getReviewsWithImages().doubleValue() / stats.getTotalReviews().doubleValue()) * 100
                    : 0.0;

            return StatisticsResponse.builder()
                    .totalReviews(stats.getTotalReviews())
                    .averageRating(Math.round(stats.getAverageRating() * 10) / 10.0) // 소수점 1자리
                    .ratingDistribution(stats.getRatingCounts())
                    .reviewsWithImages(stats.getReviewsWithImages())
                    .imageReviewRatio(Math.round(imageRatio * 10) / 10.0) // 소수점 1자리
                    .build();
        }
    }

    // =================================================================
    // 페이징 정보 DTO
    // =================================================================

    @Schema(description = "페이징 정보")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        private Integer currentPage;

        @Schema(description = "페이지 크기", example = "20")
        private Integer pageSize;

        @Schema(description = "총 요소 수", example = "150")
        private Long totalElements;

        @Schema(description = "총 페이지 수", example = "8")
        private Integer totalPages;

        @Schema(description = "첫 번째 페이지 여부", example = "true")
        private Boolean isFirst;

        @Schema(description = "마지막 페이지 여부", example = "false")
        private Boolean isLast;

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private Boolean hasNext;

        @Schema(description = "이전 페이지 존재 여부", example = "false")
        private Boolean hasPrevious;

        public static PageInfo fromPage(org.springframework.data.domain.Page<?> page) {
            return PageInfo.builder()
                    .currentPage(page.getNumber())
                    .pageSize(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .isFirst(page.isFirst())
                    .isLast(page.isLast())
                    .hasNext(page.hasNext())
                    .hasPrevious(page.hasPrevious())
                    .build();
        }
    }

    // =================================================================
    // 검색/필터 조건 DTO
    // =================================================================

    @Schema(description = "리뷰 검색 및 필터 조건")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterRequest {

        @Schema(description = "상품 ID", example = "1")
        private Long productId;

        @Schema(description = "별점 필터 (1~5, null이면 전체)", example = "5")
        @Min(value = 1, message = "별점은 1점부터 5점까지 선택할 수 있습니다.")
        @Max(value = 5, message = "별점은 1점부터 5점까지 선택할 수 있습니다.")
        private Integer rating;

        @Schema(description = "기간 필터", example = "WEEK", allowableValues = {"WEEK", "MONTH", "YEAR", "ALL"})
        private PeriodFilter period;

        @Schema(description = "이미지 필터", example = "WITH_IMAGE", allowableValues = {"ALL", "WITH_IMAGE", "TEXT_ONLY"})
        private ImageFilter imageFilter;

        @Schema(description = "정렬 방식", example = "LATEST", allowableValues = {"LATEST", "OLDEST", "RATING_HIGH", "RATING_LOW"})
        private SortType sortType;

        @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
        private Integer page;

        @Schema(description = "페이지 크기", example = "20")
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
        private Integer size;
    }

    // =================================================================
    // Enum 클래스들
    // =================================================================

    @Schema(description = "기간 필터")
    public enum PeriodFilter {
        ALL("전체"),
        WEEK("최근 일주일"),
        MONTH("최근 한달"),
        YEAR("최근 일년");

        private final String description;

        PeriodFilter(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Schema(description = "이미지 필터")
    public enum ImageFilter {
        ALL("전체"),
        WITH_IMAGE("사진 리뷰만"),
        TEXT_ONLY("글 리뷰만");

        private final String description;

        ImageFilter(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Schema(description = "정렬 방식")
    public enum SortType {
        LATEST("최신순"),
        OLDEST("오래된순"),
        RATING_HIGH("별점 높은순"),
        RATING_LOW("별점 낮은순");

        private final String description;

        SortType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}