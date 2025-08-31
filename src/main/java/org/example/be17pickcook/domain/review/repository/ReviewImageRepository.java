package org.example.be17pickcook.domain.review.repository;

import org.example.be17pickcook.domain.review.model.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 리뷰 이미지 Repository
 * - 기본 CRUD + 리뷰별 이미지 관리
 */
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    /**
     * 특정 리뷰의 이미지들을 순서대로 조회
     */
    List<ReviewImage> findByReviewReviewIdOrderByImageOrderAsc(Long reviewId);

    /**
     * 특정 리뷰의 특정 순서 이미지 조회
     */
    ReviewImage findByReviewReviewIdAndImageOrder(Long reviewId, Integer imageOrder);

    /**
     * 특정 리뷰의 이미지 개수
     */
    @Query("SELECT COUNT(ri) FROM ReviewImage ri WHERE ri.review.reviewId = :reviewId")
    Long countByReviewId(@Param("reviewId") Long reviewId);

    /**
     * 특정 리뷰의 모든 이미지 URL 조회
     */
    @Query("SELECT ri.imageUrl FROM ReviewImage ri " +
            "WHERE ri.review.reviewId = :reviewId " +
            "ORDER BY ri.imageOrder ASC")
    List<String> findImageUrlsByReviewId(@Param("reviewId") Long reviewId);

    /**
     * S3 URL로 이미지 조회 (삭제용)
     */
    @Query("SELECT ri FROM ReviewImage ri WHERE ri.imageUrl = :imageUrl")
    List<ReviewImage> findByImageUrl(@Param("imageUrl") String imageUrl);

    /**
     * 특정 리뷰의 이미지를 모두 삭제
     */
    void deleteByReviewReviewId(Long reviewId);

    /**
     * 특정 리뷰의 특정 순서 이미지 삭제
     */
    void deleteByReviewReviewIdAndImageOrder(Long reviewId, Integer imageOrder);
}