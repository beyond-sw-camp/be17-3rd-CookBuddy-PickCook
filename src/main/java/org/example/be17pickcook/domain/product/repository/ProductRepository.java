package org.example.be17pickcook.domain.product.repository;

import org.example.be17pickcook.domain.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    // =================================================================
    // 기존 메서드들
    // =================================================================
    Page<Product> findAll(Pageable pageable);

    // =================================================================
    // 리뷰 통계 포함 상품 목록 조회 (수정됨)
    // =================================================================

    /**
     * 상품 목록과 실시간 리뷰 통계를 함께 조회
     * @param pageable 페이징 정보
     * @return [product_id, title, main_image_url, discount_rate, original_price, review_count, average_rating]
     */
    @Query("SELECT p.id, p.title, p.main_image_url, p.discount_rate, p.original_price, " +
            "COALESCE(COUNT(r), 0) as review_count, " +
            "COALESCE(AVG(CAST(r.rating AS double)), 0.0) as average_rating " +
            "FROM Product p " +
            "LEFT JOIN Review r ON r.product.id = p.id AND r.isDeleted = false " +
            "GROUP BY p.id, p.title, p.main_image_url, p.discount_rate, p.original_price")
    Page<Object[]> findAllProductListWithReviewStats(Pageable pageable);

    // =================================================================
    // 카테고리 필터 관련 메서드 (수정됨)
    // =================================================================

    /**
     * 카테고리명으로 상품 조회 (페이징 및 정렬 지원) - 리뷰 통계 포함
     * @param categoryName 카테고리명 (예: "채소", "정육·가공육·달걀")
     * @param pageable 페이징 및 정렬 정보
     * @return [product_id, title, main_image_url, discount_rate, original_price, review_count, average_rating]
     */
    @Query("SELECT p.id, p.title, p.main_image_url, p.discount_rate, p.original_price, " +
            "COALESCE(COUNT(r), 0) as review_count, " +
            "COALESCE(AVG(CAST(r.rating AS double)), 0.0) as average_rating " +
            "FROM Product p " +
            "LEFT JOIN Review r ON r.product.id = p.id AND r.isDeleted = false " +
            "WHERE p.category = :categoryName " +
            "GROUP BY p.id, p.title, p.main_image_url, p.discount_rate, p.original_price")
    Page<Object[]> findByCategoryWithReviewStats(@Param("categoryName") String categoryName, Pageable pageable);

    /**
     * 기존 메서드 (Entity 조회용) - 상세 페이지용
     */
    @Query("SELECT p FROM Product p WHERE p.category = :categoryName")
    Page<Product> findByCategory(@Param("categoryName") String categoryName, Pageable pageable);
}