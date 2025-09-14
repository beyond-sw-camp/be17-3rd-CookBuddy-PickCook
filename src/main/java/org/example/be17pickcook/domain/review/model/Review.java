package org.example.be17pickcook.domain.review.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.be17pickcook.common.BaseEntity;
import org.example.be17pickcook.domain.order.model.Orders;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.user.model.User;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 리뷰 엔티티
 * - 실제 DB 스키마(reviews 테이블)와 완전 매핑
 * - 소프트 삭제 지원 (is_deleted, deleted_at)
 * - ReviewImage와 일대다 연관관계
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    // =================================================================
    // 기본 필드
    // =================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    // =================================================================
    // 소프트 삭제 필드 (실제 DB 스키마 기준)
    // =================================================================

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BIT(1) DEFAULT 0")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // =================================================================
    // 연관관계 매핑
    // =================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 리뷰 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;  // 리뷰 대상 상품

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)  // N+1 방지
    @Builder.Default
    private List<ReviewImage> images = new ArrayList<>();

    // =================================================================
    // 비즈니스 로직 메서드
    // =================================================================

    /**
     * 리뷰 이미지 추가
     */
    public void addImage(ReviewImage image) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(image);
        image.setReview(this);
    }

    /**
     * 리뷰 이미지 제거
     */
    public void removeImage(ReviewImage image) {
        if (this.images != null) {
            this.images.remove(image);
            image.setReview(null);
        }
    }

    /**
     * 소프트 삭제 처리
     */
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 소프트 삭제 복원 (실행 취소용)
     */
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }

    /**
     * 리뷰 내용 수정
     */
    public void updateContent(String content, Integer rating) {
        if (content != null && !content.trim().isEmpty()) {
            this.content = content.trim();
        }
        if (rating != null && rating >= 1 && rating <= 5) {
            this.rating = rating;
        }
    }

    /**
     * 리뷰 수정 가능한지 확인 (작성 후 7일 이내)
     */
    public boolean isModifiable() {
        if (this.getCreatedAt() == null) return false;
        return this.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7));
    }

    /**
     * 특정 사용자가 작성한 리뷰인지 확인
     */
    public boolean isWrittenBy(Integer userId) {
        return this.user != null && this.user.getIdx().equals(userId);
    }

    /**
     * 이미지가 있는 리뷰인지 확인
     */
    public boolean hasImages() {
        return this.images != null && !this.images.isEmpty();
    }

    /**
     * 이미지 개수 반환
     */
    public int getImageCount() {
        return this.images != null ? this.images.size() : 0;
    }
}