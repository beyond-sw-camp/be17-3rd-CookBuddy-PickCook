package org.example.be17pickcook.domain.review.repository;

import org.example.be17pickcook.domain.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 리뷰 Repository
 * - 기본 CRUD + 상품별/사용자별 조회
 * - 소프트 삭제 고려한 조회 메서드들
 */
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {

    // =================================================================
    // 기본 조회 메서드 (소프트 삭제 필터링)
    // =================================================================

    /**
     * 삭제되지 않은 리뷰만 조회 (ID로)
     */
    Optional<Review> findByReviewIdAndIsDeletedFalse(Long reviewId);

    /**
     * 특정 상품의 삭제되지 않은 리뷰만 조회
     */
    @Query("SELECT r FROM Review r " +
            "WHERE r.product.id = :productId AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByProductIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("productId") Long productId);

    /**
     * 특정 사용자의 삭제되지 않은 리뷰만 조회
     */
    List<Review> findByUserIdxAndIsDeletedFalseOrderByCreatedAtDesc(Integer userId);

    /**
     * 특정 상품에 대한 특정 사용자의 리뷰 조회 (중복 체크용)
     */
    Optional<Review> findByProductIdAndUserIdxAndIsDeletedFalse(Long productId, Integer userId);

    // =================================================================
    // 통계 조회 메서드
    // =================================================================

    /**
     * 특정 상품의 총 리뷰 수
     */
    @Query("SELECT COUNT(r) FROM Review r " +
            "WHERE r.product.id = :productId AND r.isDeleted = false")
    Long countByProductIdAndIsDeletedFalse(@Param("productId") Long productId);

    /**
     * 특정 상품의 평균 별점
     */
    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r " +
            "WHERE r.product.id = :productId AND r.isDeleted = false")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    /**
     * 특정 상품의 별점별 개수 (1~5점)
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r " +
            "WHERE r.product.id = :productId AND r.isDeleted = false " +
            "GROUP BY r.rating " +
            "ORDER BY r.rating")
    List<Object[]> getRatingDistributionByProductId(@Param("productId") Long productId);

    // =================================================================
    // 페이징 조회 메서드
    // =================================================================

    /**
     * 특정 상품의 리뷰 페이징 조회
     */
    Page<Review> findByProductIdAndIsDeletedFalse(Long productId, Pageable pageable);

    /**
     * 특정 사용자의 리뷰 페이징 조회
     */
    Page<Review> findByUserIdxAndIsDeletedFalse(Integer userId, Pageable pageable);

    // =================================================================
    // 별점 필터링 메서드
    // =================================================================

    /**
     * 특정 상품의 특정 별점 리뷰만 조회
     */
    @Query("SELECT r FROM Review r " +
            "WHERE r.product.id = :productId " +
            "AND r.rating = :rating " +
            "AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByProductIdAndRatingAndIsDeletedFalse(
            @Param("productId") Long productId,
            @Param("rating") Integer rating);

    // =================================================================
    // 기간 필터링 메서드
    // =================================================================

    /**
     * 특정 상품의 특정 기간 이후 리뷰 조회
     */
    @Query("SELECT r FROM Review r " +
            "WHERE r.product.id = :productId " +
            "AND r.createdAt >= :startDate " +
            "AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByProductIdAndCreatedAtAfterAndIsDeletedFalse(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate);

    // =================================================================
    // 이미지 필터링 메서드
    // =================================================================

    /**
     * 특정 상품의 이미지가 있는 리뷰만 조회
     */
    @Query("SELECT r FROM Review r " +
            "WHERE r.product.id = :productId " +
            "AND r.isDeleted = false " +
            "AND EXISTS (SELECT 1 FROM ReviewImage ri WHERE ri.review = r) " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByProductIdWithImagesAndIsDeletedFalse(@Param("productId") Long productId);

    /**
     * 특정 상품의 이미지가 없는 리뷰만 조회 (텍스트만)
     */
    @Query("SELECT r FROM Review r " +
            "WHERE r.product.id = :productId " +
            "AND r.isDeleted = false " +
            "AND NOT EXISTS (SELECT 1 FROM ReviewImage ri WHERE ri.review = r) " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByProductIdWithoutImagesAndIsDeletedFalse(@Param("productId") Long productId);

    // =================================================================
    // 내 리뷰 우선 표시용 메서드
    // =================================================================

    /**
     * 특정 상품의 리뷰를 내 리뷰 우선으로 정렬 조회
     */
    @Query("SELECT r FROM Review r " +
            "WHERE r.product.id = :productId AND r.isDeleted = false " +
            "ORDER BY " +
            "CASE WHEN r.user.idx = :currentUserId THEN 0 ELSE 1 END, " +
            "r.createdAt DESC")
    List<Review> findByProductIdWithMyReviewFirst(
            @Param("productId") Long productId,
            @Param("currentUserId") Integer currentUserId);

    // =================================================================
    // 권한 체크 메서드 (향후 주문 연동용)
    // =================================================================

    /**
     * 특정 사용자가 특정 상품을 구매했는지 확인 (향후 구현)
     */
    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.user.idx = :userId " +
            "AND oi.product.id = :productId " +
            "AND o.status = 'COMPLETED'")
    Boolean hasUserPurchasedProduct(@Param("userId") Integer userId, @Param("productId") Long productId);

    // =================================================================
    // 관리자용 메서드
    // =================================================================

    /**
     * 모든 리뷰 조회 (삭제된 것 포함, 관리자용)
     */
    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    Page<Review> findAllIncludingDeleted(Pageable pageable);

    /**
     * 삭제된 리뷰만 조회 (관리자용)
     */
    @Query("SELECT r FROM Review r WHERE r.isDeleted = true ORDER BY r.deletedAt DESC")
    List<Review> findDeletedReviews();
}