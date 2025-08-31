package org.example.be17pickcook.domain.review.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.be17pickcook.common.BaseEntity;

/**
 * 리뷰 이미지 엔티티
 * - 실제 DB 스키마(review_images 테이블)와 완전 매핑
 * - S3 URL과 메타데이터 관리
 * - 이미지 순서 관리 (image_order)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "review_images")
public class ReviewImage extends BaseEntity {

    // =================================================================
    // 기본 필드
    // =================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;  // S3 URL

    @Column(name = "original_filename", length = 255)
    private String originalFilename;  // 원본 파일명

    @Column(name = "content_type", length = 10)
    private String contentType;  // MIME 타입 (image/jpeg, image/png 등)

    @Column(name = "file_size")
    private Long fileSize;  // 파일 크기 (bytes)

    @Column(name = "image_order", nullable = false)
    private Integer imageOrder;  // 이미지 순서 (1~5)

    // =================================================================
    // 연관관계 매핑
    // =================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;  // 소속 리뷰

    // =================================================================
    // 비즈니스 로직 메서드
    // =================================================================

    /**
     * 이미지 메타데이터 설정
     */
    public void setImageMetadata(String originalFilename, String contentType, Long fileSize) {
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    /**
     * 이미지 순서 설정
     */
    public void setOrder(Integer order) {
        if (order != null && order >= 1 && order <= 5) {
            this.imageOrder = order;
        }
    }

    /**
     * 썸네일 URL 생성 (S3 URL 기반)
     */
    public String getThumbnailUrl() {
        if (this.imageUrl == null) return null;

        // S3 URL에 썸네일 파라미터 추가하는 로직
        // 예: https://bucket.s3.amazonaws.com/image.jpg -> https://bucket.s3.amazonaws.com/image_thumb.jpg
        return this.imageUrl.replace(".jpg", "_thumb.jpg")
                .replace(".jpeg", "_thumb.jpeg")
                .replace(".png", "_thumb.png")
                .replace(".webp", "_thumb.webp");
    }

    /**
     * 이미지 형식이 지원되는지 확인
     */
    public boolean isSupportedFormat() {
        if (this.contentType == null) return false;

        return this.contentType.equals("image/jpeg") ||
                this.contentType.equals("image/jpg") ||
                this.contentType.equals("image/png") ||
                this.contentType.equals("image/webp");
    }

    /**
     * 파일 크기가 허용 범위인지 확인 (10MB 이하)
     */
    public boolean isValidFileSize() {
        if (this.fileSize == null) return false;
        final long MAX_SIZE = 10 * 1024 * 1024; // 10MB
        return this.fileSize <= MAX_SIZE;
    }

    /**
     * 이미지 파일명에서 확장자 추출
     */
    public String getFileExtension() {
        if (this.originalFilename == null) return null;

        int dotIndex = this.originalFilename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < this.originalFilename.length() - 1) {
            return this.originalFilename.substring(dotIndex + 1).toLowerCase();
        }
        return null;
    }
}