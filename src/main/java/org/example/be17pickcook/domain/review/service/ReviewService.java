package org.example.be17pickcook.domain.review.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.exception.BaseException;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.domain.order.model.OrderItem;
import org.example.be17pickcook.domain.order.repository.OrderRepository;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.product.repository.ProductRepository;
import org.example.be17pickcook.domain.review.mapper.ReviewMapper;
import org.example.be17pickcook.domain.review.model.Review;
import org.example.be17pickcook.domain.review.model.ReviewDto;
import org.example.be17pickcook.domain.review.model.ReviewImage;
import org.example.be17pickcook.domain.review.repository.ReviewRepository;
import org.example.be17pickcook.domain.review.repository.ReviewImageRepository;
import org.example.be17pickcook.domain.review.repository.ReviewRepositoryCustom;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewMapper reviewMapper;

    // =================================================================
    // 리뷰 작성
    // =================================================================

    @Transactional
    public ReviewDto.Response createReview(Integer userId, ReviewDto.WriteRequest dto) {
        // =================================================================
        // 1. 권한 검증 (구매 이력 + 중복 방지 + 기한 확인)
        // =================================================================
        validateReviewWritePermission(userId, dto.getProductId());

        // =================================================================
        // 2. 사용자 및 상품 정보 조회
        // =================================================================
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.USER_NOT_FOUND));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.RESOURCE_NOT_FOUND));

        // =================================================================
        // 3. 이미지 개수 검증
        // =================================================================
        if (dto.getImageUrls() != null && dto.getImageUrls().size() > 5) {
            throw BaseException.from(BaseResponseStatus.REVIEW_IMAGE_COUNT_EXCEEDED);
        }

        // =================================================================
        // 4. MapStruct로 리뷰 엔티티 생성
        // =================================================================
        Review review = reviewMapper.writeRequestToEntity(dto);
        review.setUser(user);
        review.setProduct(product);

        Review savedReview = reviewRepository.save(review);

        // =================================================================
        // 5. 이미지 처리 (순서 보장)
        // =================================================================
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            for (int i = 0; i < dto.getImageUrls().size(); i++) {
                ReviewImage image = ReviewImage.builder()
                        .review(savedReview)
                        .imageUrl(dto.getImageUrls().get(i))
                        .imageOrder(i + 1)
                        .build();
                reviewImageRepository.save(image);
                savedReview.addImage(image);
            }
        }

        // =================================================================
        // 6. MapStruct로 응답 DTO 생성
        // =================================================================
        return reviewMapper.entityToResponse(savedReview, userId);
    }

    // =================================================================
    // 리뷰 수정
    // =================================================================

    @Transactional
    public ReviewDto.Response updateReview(Long reviewId, Integer userId, ReviewDto.UpdateRequest dto) {
        Review review = reviewRepository.findByReviewIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.REVIEW_NOT_FOUND));

        // 권한 확인
        if (!review.isWrittenBy(userId)) {
            throw BaseException.from(BaseResponseStatus.REVIEW_AUTHOR_MISMATCH);
        }

        // 수정 기간 확인 (7일 이내)
        if (!review.isModifiable()) {
            throw BaseException.from(BaseResponseStatus.REVIEW_MODIFICATION_PERIOD_EXPIRED);
        }

        // 내용 수정
        review.updateContent(dto.getTitle(), dto.getContent(), dto.getRating());

        // 이미지 수정 (기존 이미지 삭제 후 새로 추가)
        if (dto.getImageUrls() != null) {
            // 기존 이미지 삭제
            reviewImageRepository.deleteByReviewReviewId(reviewId);
            review.getImages().clear();

            // 새 이미지 추가
            for (int i = 0; i < dto.getImageUrls().size() && i < 5; i++) {
                ReviewImage image = ReviewImage.builder()
                        .review(review)
                        .imageUrl(dto.getImageUrls().get(i))
                        .imageOrder(i + 1)
                        .build();
                reviewImageRepository.save(image);
                review.addImage(image);
            }
        }

        return ReviewDto.Response.fromEntityWithUserContext(review, userId);
    }

    // =================================================================
    // 리뷰 삭제
    // =================================================================

    @Transactional
    public void deleteReview(Long reviewId, Integer userId) {
        Review review = reviewRepository.findByReviewIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.REVIEW_NOT_FOUND));

        // 권한 확인
        if (!review.isWrittenBy(userId)) {
            throw BaseException.from(BaseResponseStatus.REVIEW_AUTHOR_MISMATCH);
        }

        // 소프트 삭제
        review.softDelete();
    }

    // =================================================================
    // 상품별 리뷰 조회
    // =================================================================

    public ReviewDto.ListResponse getProductReviews(ReviewDto.FilterRequest filter, Integer currentUserId) {
        // =================================================================
        // 1. 필터 조건 변환
        // =================================================================
        LocalDateTime startDate = getStartDateByPeriod(filter.getPeriod());
        Boolean hasImages = getHasImagesByFilter(filter.getImageFilter());
        String sortType = getSortTypeString(filter.getSortType());

        // =================================================================
        // 2. QueryDSL 페이징 조회
        // =================================================================
        Pageable pageable = PageRequest.of(
                filter.getPage() != null ? filter.getPage() : 0,
                filter.getSize() != null ? filter.getSize() : 20
        );

        Page<Review> reviewPage = reviewRepository.findByComplexFilterWithPaging(
                filter.getProductId(),
                filter.getRating(),
                startDate,
                hasImages,
                sortType,
                currentUserId,
                pageable
        );

        // =================================================================
        // 3. MapStruct로 DTO 변환 + 내 리뷰 표시
        // =================================================================
        List<ReviewDto.Response> reviews = reviewPage.getContent().stream()
                .map(review -> ReviewDto.Response.fromEntityWithUserContext(review, currentUserId))
                .toList();

        // =================================================================
        // 4. 리뷰 통계 조회 (첫 페이지일 때만)
        // =================================================================
        ReviewRepositoryCustom.ReviewStatistics statistics = null;
        if (filter.getPage() == null || filter.getPage() == 0) {
            statistics = reviewRepository.getReviewStatistics(filter.getProductId());
        }

        // =================================================================
        // 5. 응답 생성
        // =================================================================
        return ReviewDto.ListResponse.builder()
                .reviews(reviews)
                .statistics(convertToStatisticsDto(statistics))
                .pageInfo(ReviewDto.PageInfo.fromPage(reviewPage))
                .build();
    }

    // =================================================================
    // 내 리뷰 조회
    // =================================================================

    public ReviewDto.ListResponse getMyReviews(Integer userId, ReviewDto.FilterRequest filter) {
        String sortType = getSortTypeString(filter.getSortType());

        List<Review> userReviews = reviewRepository.findUserReviews(
                userId, null, null, sortType);

        // 페이징 처리 (메모리에서)
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;
        int start = page * size;
        int end = Math.min(start + size, userReviews.size());

        List<Review> pagedReviews = userReviews.subList(start, end);

        // MapStruct로 DTO 변환
        List<ReviewDto.Response> reviews = pagedReviews.stream()
                .map(review -> ReviewDto.Response.fromEntityWithUserContext(review, userId))
                .toList();

        // 수동 페이지 정보 생성
        ReviewDto.PageInfo pageInfo = ReviewDto.PageInfo.builder()
                .currentPage(page)
                .pageSize(size)
                .totalElements((long) userReviews.size())
                .totalPages((int) Math.ceil((double) userReviews.size() / size))
                .isFirst(page == 0)
                .isLast(end >= userReviews.size())
                .hasNext(end < userReviews.size())
                .hasPrevious(page > 0)
                .build();

        return ReviewDto.ListResponse.builder()
                .reviews(reviews)
                .statistics(null)
                .pageInfo(pageInfo)
                .build();
    }

    // =================================================================
    // 유틸리티 메서드
    // =================================================================

    private LocalDateTime getStartDateByPeriod(ReviewDto.PeriodFilter period) {
        if (period == null || period == ReviewDto.PeriodFilter.ALL) return null;

        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case WEEK -> now.minusWeeks(1);
            case MONTH -> now.minusMonths(1);
            case YEAR -> now.minusYears(1);
            default -> null;
        };
    }

    private Boolean getHasImagesByFilter(ReviewDto.ImageFilter imageFilter) {
        if (imageFilter == null || imageFilter == ReviewDto.ImageFilter.ALL) return null;
        return imageFilter == ReviewDto.ImageFilter.WITH_IMAGE;
    }

    private String getSortTypeString(ReviewDto.SortType sortType) {
        if (sortType == null) return "latest";
        return switch (sortType) {
            case LATEST -> "latest";
            case OLDEST -> "oldest";
            case RATING_HIGH -> "rating_high";
            case RATING_LOW -> "rating_low";
        };
    }

    /**
     * ReviewStatistics를 DTO로 변환
     */
    private ReviewDto.StatisticsResponse convertToStatisticsDto(
            ReviewRepositoryCustom.ReviewStatistics statistics) {

        if (statistics == null) {
            return ReviewDto.StatisticsResponse.builder()
                    .totalReviews(0L)
                    .averageRating(0.0)
                    .ratingDistribution(new Long[]{0L, 0L, 0L, 0L, 0L})
                    .reviewsWithImages(0L)
                    .imageReviewRatio(0.0)
                    .build();
        }

        Double imageRatio = statistics.getTotalReviews() > 0
                ? (statistics.getReviewsWithImages().doubleValue() / statistics.getTotalReviews().doubleValue()) * 100
                : 0.0;

        return ReviewDto.StatisticsResponse.builder()
                .totalReviews(statistics.getTotalReviews())
                .averageRating(Math.round(statistics.getAverageRating() * 10.0) / 10.0)
                .ratingDistribution(statistics.getRatingCounts())
                .reviewsWithImages(statistics.getReviewsWithImages())
                .imageReviewRatio(Math.round(imageRatio * 10.0) / 10.0)
                .build();
    }

    // =================================================================
    // 권한 검증 메서드
    // =================================================================

    /**
     * 리뷰 작성 권한 검증
     */
    private void validateReviewWritePermission(Integer userId, Long productId) {
        // 1. 구매 이력 확인
        Optional<OrderItem> purchaseHistory = orderRepository
                .findCompletedOrderItemByUserAndProduct(userId, productId);

        if (purchaseHistory.isEmpty()) {
            throw BaseException.from(BaseResponseStatus.REVIEW_NO_PURCHASE_HISTORY);
        }

        // 2. 중복 리뷰 방지
        Optional<Review> existingReview = reviewRepository
                .findByProductIdAndUserIdxAndIsDeletedFalse(productId, userId);

        if (existingReview.isPresent()) {
            throw BaseException.from(BaseResponseStatus.REVIEW_DUPLICATE_NOT_ALLOWED);
        }

        // 3. 작성 기한 검증 (구매 후 6개월)
        LocalDateTime purchaseDate = purchaseHistory.get().getCreatedAt();
        LocalDateTime expirationDate = purchaseDate.plusMonths(6);

        if (LocalDateTime.now().isAfter(expirationDate)) {
            throw BaseException.from(BaseResponseStatus.REVIEW_PURCHASE_DATE_INVALID);
        }
    }

    /**
     * 리뷰 수정 권한 검증
     */
    private void validateReviewUpdatePermission(Review review, Integer userId) {
        // 본인 작성 확인
        if (!review.isWrittenBy(userId)) {
            throw BaseException.from(BaseResponseStatus.REVIEW_AUTHOR_MISMATCH);
        }

        // 7일 이내 수정 제한
        LocalDateTime createdAt = review.getCreatedAt();
        LocalDateTime updateDeadline = createdAt.plusDays(7);

        if (LocalDateTime.now().isAfter(updateDeadline)) {
            throw BaseException.from(BaseResponseStatus.REVIEW_MODIFICATION_PERIOD_EXPIRED);
        }
    }
}