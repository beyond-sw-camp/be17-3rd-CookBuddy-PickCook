package org.example.be17pickcook.domain.review.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.example.be17pickcook.domain.review.model.QReview.review;
import static org.example.be17pickcook.domain.review.model.QReviewImage.reviewImage;

/**
 * 리뷰 커스텀 리포지토리 구현체
 * - QueryDSL을 활용한 동적 쿼리 및 복합 필터링
 */
@Repository
@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // =================================================================
    // 복합 필터 조회 메서드
    // =================================================================

    @Override
    public List<Review> findByComplexFilter(
            Long productId,
            Integer rating,
            LocalDateTime startDate,
            Boolean hasImages,
            String sortType,
            Integer currentUserId) {

        return queryFactory
                .selectFrom(review)
                .leftJoin(review.images, reviewImage).fetchJoin()  // N+1 방지
                .where(
                        review.isDeleted.eq(false),
                        productIdEq(productId),
                        ratingEq(rating),
                        createdAtAfter(startDate),
                        hasImagesCondition(hasImages)
                )
                .orderBy(createOrderSpecifier(sortType, currentUserId))
                .fetch();
    }

    @Override
    public Page<Review> findByComplexFilterWithPaging(
            Long productId,
            Integer rating,
            LocalDateTime startDate,
            Boolean hasImages,
            String sortType,
            Integer currentUserId,
            Pageable pageable) {

        JPAQuery<Review> query = queryFactory
                .selectFrom(review)
                .leftJoin(review.images, reviewImage).fetchJoin()
                .where(
                        review.isDeleted.eq(false),
                        productIdEq(productId),
                        ratingEq(rating),
                        createdAtAfter(startDate),
                        hasImagesCondition(hasImages)
                )
                .orderBy(createOrderSpecifier(sortType, currentUserId));

        // 페이징 적용
        List<Review> results = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회
        Long total = queryFactory
                .select(review.count())
                .from(review)
                .where(
                        review.isDeleted.eq(false),
                        productIdEq(productId),
                        ratingEq(rating),
                        createdAtAfter(startDate),
                        hasImagesCondition(hasImages)
                )
                .fetchOne();

        return new PageImpl<>(results, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Review> findUserReviews(
            Integer userId,
            Long productId,
            Integer rating,
            String sortType) {

        return queryFactory
                .selectFrom(review)
                .leftJoin(review.images, reviewImage).fetchJoin()
                .where(
                        review.isDeleted.eq(false),
                        review.user.idx.eq(userId),
                        productIdEq(productId),
                        ratingEq(rating)
                )
                .orderBy(createOrderSpecifier(sortType, null))
                .fetch();
    }

    @Override
    public ReviewStatistics getReviewStatistics(Long productId) {

        // 총 리뷰 수
        Long totalReviews = queryFactory
                .select(review.count())
                .from(review)
                .where(review.product.id.eq(productId), review.isDeleted.eq(false))
                .fetchOne();

        // 평균 별점
        Double averageRating = queryFactory
                .select(review.rating.avg())
                .from(review)
                .where(review.product.id.eq(productId), review.isDeleted.eq(false))
                .fetchOne();

        // 별점별 개수 조회
        Long[] ratingCounts = new Long[5]; // [1점, 2점, 3점, 4점, 5점]
        for (int i = 1; i <= 5; i++) {
            Long count = queryFactory
                    .select(review.count())
                    .from(review)
                    .where(
                            review.product.id.eq(productId),
                            review.isDeleted.eq(false),
                            review.rating.eq(i)
                    )
                    .fetchOne();
            ratingCounts[i-1] = count != null ? count : 0L;
        }

        // 이미지 있는 리뷰 수
        Long reviewsWithImages = queryFactory
                .select(review.countDistinct())
                .from(review)
                .join(review.images, reviewImage)
                .where(review.product.id.eq(productId), review.isDeleted.eq(false))
                .fetchOne();

        return new ReviewStatistics(
                totalReviews != null ? totalReviews : 0L,
                averageRating != null ? averageRating : 0.0,
                ratingCounts,
                reviewsWithImages != null ? reviewsWithImages : 0L
        );
    }

    // =================================================================
    // 조건별 private 메서드들
    // =================================================================

    /**
     * 상품 ID 조건
     */
    private BooleanExpression productIdEq(Long productId) {
        return productId != null ? review.product.id.eq(productId) : null;
    }

    /**
     * 별점 조건
     */
    private BooleanExpression ratingEq(Integer rating) {
        return rating != null ? review.rating.eq(rating) : null;
    }

    /**
     * 생성일 이후 조건
     */
    private BooleanExpression createdAtAfter(LocalDateTime startDate) {
        return startDate != null ? review.createdAt.after(startDate) : null;
    }

    /**
     * 이미지 존재 조건
     */
    private BooleanExpression hasImagesCondition(Boolean hasImages) {
        if (hasImages == null) {
            return null; // 필터 조건 없음
        }

        if (hasImages) {
            // 이미지가 있는 리뷰 - EXISTS 서브쿼리 사용
            return review.images.size().gt(0);
        } else {
            // 이미지가 없는 리뷰 - 크기가 0이거나 빈 컬렉션
            return review.images.size().eq(0);
        }
    }

    /**
     * 정렬 조건 생성
     */
    private OrderSpecifier<?>[] createOrderSpecifier(String sortType, Integer currentUserId) {
        if (sortType == null) sortType = "latest";

        OrderSpecifier<?> primaryOrder;
        OrderSpecifier<?> secondaryOrder = review.createdAt.desc(); // 기본 2차 정렬

        switch (sortType) {
            case "oldest":
                primaryOrder = review.createdAt.asc();
                secondaryOrder = review.reviewId.asc();
                break;
            case "rating_high":
                primaryOrder = review.rating.desc();
                break;
            case "rating_low":
                primaryOrder = review.rating.asc();
                break;
            case "latest":
            default:
                primaryOrder = review.createdAt.desc();
                break;
        }

        // 내 리뷰 우선 정렬 (로그인한 사용자가 있는 경우)
        if (currentUserId != null) {
            OrderSpecifier<?> myReviewFirst = new CaseBuilder()
                    .when(review.user.idx.eq(currentUserId)).then(0)
                    .otherwise(1)
                    .asc();

            return new OrderSpecifier[]{myReviewFirst, primaryOrder, secondaryOrder};
        }

        return new OrderSpecifier[]{primaryOrder, secondaryOrder};
    }
}