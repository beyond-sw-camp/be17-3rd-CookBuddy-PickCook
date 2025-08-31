package org.example.be17pickcook.domain.review.repository;

import org.example.be17pickcook.domain.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 리뷰 커스텀 리포지토리 인터페이스
 * - QueryDSL을 사용한 복합 필터링 및 동적 쿼리
 */
public interface ReviewRepositoryCustom {

    /**
     * 복합 필터로 리뷰 조회 (QueryDSL)
     * - 상품별 + 별점 필터 + 기간 필터 + 이미지 필터 + 정렬
     */
    List<Review> findByComplexFilter(
            Long productId,
            Integer rating,           // null이면 전체, 1~5면 해당 별점만
            LocalDateTime startDate,  // null이면 전체, 있으면 해당 날짜 이후
            Boolean hasImages,        // null이면 전체, true면 이미지 있는 것, false면 텍스트만
            String sortType,          // "latest", "oldest", "rating_high", "rating_low"
            Integer currentUserId     // null이면 일반 정렬, 있으면 내 리뷰 우선
    );

    /**
     * 복합 필터로 리뷰 페이징 조회 (QueryDSL)
     */
    Page<Review> findByComplexFilterWithPaging(
            Long productId,
            Integer rating,
            LocalDateTime startDate,
            Boolean hasImages,
            String sortType,
            Integer currentUserId,
            Pageable pageable
    );

    /**
     * 사용자별 리뷰 조회 (복합 필터)
     */
    List<Review> findUserReviews(
            Integer userId,
            Long productId,      // null이면 전체 상품
            Integer rating,      // null이면 전체 별점
            String sortType
    );

    /**
     * 리뷰 통계 조회 (특정 상품)
     */
    ReviewStatistics getReviewStatistics(Long productId);

    /**
     * 내부 클래스: 리뷰 통계 결과
     */
    class ReviewStatistics {
        private Long totalReviews;
        private Double averageRating;
        private Long[] ratingCounts; // [1점, 2점, 3점, 4점, 5점] 개수
        private Long reviewsWithImages;

        public ReviewStatistics(Long totalReviews, Double averageRating,
                                Long[] ratingCounts, Long reviewsWithImages) {
            this.totalReviews = totalReviews;
            this.averageRating = averageRating;
            this.ratingCounts = ratingCounts;
            this.reviewsWithImages = reviewsWithImages;
        }

        // Getters
        public Long getTotalReviews() { return totalReviews; }
        public Double getAverageRating() { return averageRating; }
        public Long[] getRatingCounts() { return ratingCounts; }
        public Long getReviewsWithImages() { return reviewsWithImages; }
    }
}