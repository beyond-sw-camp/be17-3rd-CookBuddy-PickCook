package org.example.be17pickcook.domain.product.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.be17pickcook.domain.review.model.ReviewDto;
import org.example.be17pickcook.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Product DTOs (엔티티 변환을 DTO 내부에서 처리: from / toEntity / apply)
 */
@Schema(description = "상품 관련 DTO 클래스들")
public class ProductDto {

    // ================== 리뷰 포함 응답 DTO ==================

    @Schema(description = "리뷰 정보가 포함된 상품 응답")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {

        @Schema(description = "상품 고유 ID", example = "1")
        private Long productId;

        @Schema(description = "상품 제목", example = "신선한 유기농 상추 500g")
        private String title;

        @Schema(description = "대표 이미지 URL", example = "https://example.com/product1.jpg")
        private String mainImageUrl;

        @Schema(description = "리뷰 개수", example = "23")
        private Integer reviewCount;    // 리뷰 수만 포함

        @Schema(description = "정가 (원)", example = "5000")
        private Integer originalPrice;

        @Schema(description = "할인율 (%)", example = "15")
        private Integer discountRate;

        public static ProductDto.Response fromEntity(Product product) {
            return ProductDto.Response.builder()
                    .productId(product.getId())
                    .title(product.getTitle())
                    .mainImageUrl(product.getMain_image_url())
                    .originalPrice(product.getOriginal_price())
                    .discountRate(product.getDiscount_rate())
                    // LAZY 접근 시 N+1 발생 가능
                    .reviewCount(product.getReviews().size())
                    .build();
        }

    }

    // ================== 기본 응답 DTO ==================
    @Schema(description = "상품 목록 응답 정보")
    @Getter
    @Builder
    public static class ProductListResponse {
        @Schema(description = "상품 고유 ID", example = "1")
        private Long id;

        @Schema(description = "상품 제목", example = "신선한 유기농 상추 500g")
        private String title;

        @Schema(description = "대표 이미지 URL", example = "https://example.com/product1.jpg")
        private String main_image_url;

        @Schema(description = "할인율 (%)", example = "15")
        private Integer discount_rate;

        @Schema(description = "정가 (원)", example = "5000")
        private Integer original_price;

        @Schema(description = "장바구니 담았는지 여부", example = "true")
        private Boolean isInCart;

        @Schema(description = "상품 평점", example = "5.4")
        private Integer rating;

        @Schema(description = "리뷰 수", example = "23")
        private Long review_count;

        // 장바구니 담았는지 여부
        public void setIsInCart(Boolean isInCart) {
            this.isInCart = isInCart;
        }

//        public static ProductListResponse fromEntity(Product product) {
//
//        }
    }



    @Schema(description = "상품 상세 응답 정보")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Res {

        @Schema(description = "상품 고유 ID", example = "1")
        private Long id;

        @Schema(description = "상품 카테고리", example = "채소")
        private String category;

        @Schema(description = "상품 제목", example = "신선한 유기농 상추 500g")
        private String title;

        @Schema(description = "상품 부제목", example = "아침에 따온 신선한 유기농 상추입니다")
        private String subtitle;

        @Schema(description = "대표 이미지 URL", example = "https://example.com/product1.jpg")
        private String main_image_url;

        @Schema(description = "상세 이미지 URL", example = "https://example.com/product1_detail.jpg")
        private String detail_image_url;

        @Schema(description = "판매자 이름", example = "김농부")
        private String seller;

        @Schema(description = "할인율 (%)", example = "15")
        private Integer discount_rate;

        @Schema(description = "정가 (원)", example = "5000")
        private Integer original_price;

        @Schema(description = "판매 단위", example = "1봉지")
        private String unit;

        @Schema(description = "무게/용량", example = "500g")
        private String weight_or_volume;

        @Schema(description = "유통기한", example = "2025-02-15")
        private String expiration_date; // DB가 varchar -> String 유지

        @Schema(description = "원산지", example = "국내산")
        private String origin;

        @Schema(description = "포장 방법", example = "비닐포장")
        private String packaging;

        @Schema(description = "배송 정보", example = "냉장배송")
        private String shipping_info;

        @Schema(description = "주의사항", example = "직사광선을 피해 보관하세요")
        private String notice;

        @Schema(description = "상품 설명", example = "신선하고 맛있는 유기농 상추입니다.")
        private String description;

        @Schema(description = "등록일시", example = "2025-01-15T10:30:00")
        private LocalDateTime created_at;

        @Schema(description = "수정일시", example = "2025-01-15T15:45:00")
        private LocalDateTime updated_at;

        public static Res from(Product entity) {
            return Res.builder()
                    .id(entity.getId())
                    .category(entity.getCategory())
                    .title(entity.getTitle())
                    .subtitle(entity.getSubtitle())
                    .main_image_url(entity.getMain_image_url())
                    .detail_image_url(entity.getDetail_image_url())
                    .seller(entity.getSeller())
                    .discount_rate(entity.getDiscount_rate())
                    .original_price(entity.getOriginal_price())
                    .unit(entity.getUnit())
                    .weight_or_volume(entity.getWeight_or_volume())
                    .expiration_date(entity.getExpiration_date())
                    .origin(entity.getOrigin())
                    .packaging(entity.getPackaging())
                    .shipping_info(entity.getShipping_info())
                    .notice(entity.getNotice())
                    .description(entity.getDescription())
                    .created_at(entity.getCreatedAt())
                    .updated_at(entity.getUpdatedAt())
                    .build();
        }
    }

    // =================================================================
    // 리뷰 포함 상품 상세 응답 DTO (🆕 새로 추가)
    // =================================================================

    @Schema(description = "리뷰가 포함된 상품 상세 응답")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailWithReview {

        // 기존 상품 정보
        @Schema(description = "상품 고유 ID", example = "1")
        private Long productId;

        @Schema(description = "카테고리", example = "채소")
        private String category;

        @Schema(description = "상품 제목", example = "신선한 유기농 상추 500g")
        private String title;

        @Schema(description = "상품 부제목", example = "아침에 수확한 신선한 상추")
        private String subtitle;

        @Schema(description = "대표 이미지 URL", example = "https://example.com/product1.jpg")
        private String mainImageUrl;

        @Schema(description = "상세 이미지 URL", example = "https://example.com/product1_detail.jpg")
        private String detailImageUrl;

        @Schema(description = "판매자", example = "농부마켓")
        private String seller;

        @Schema(description = "할인율 (%)", example = "15")
        private Integer discountRate;

        @Schema(description = "정가 (원)", example = "5000")
        private Integer originalPrice;

        @Schema(description = "할인된 가격 (원)", example = "4250")
        private Integer discountedPrice;

        @Schema(description = "단위", example = "1봉지")
        private String unit;

        @Schema(description = "무게/용량", example = "500g")
        private String weightOrVolume;

        @Schema(description = "유통기한", example = "수령일로부터 3일")
        private String expirationDate;

        @Schema(description = "원산지", example = "국내산")
        private String origin;

        @Schema(description = "포장타입", example = "친환경 포장")
        private String packaging;

        @Schema(description = "배송안내", example = "새벽배송 가능")
        private String shippingInfo;

        @Schema(description = "소비자안내", example = "냉장보관")
        private String notice;

        @Schema(description = "상세설명", example = "신선한 유기농 상추입니다.")
        private String description;

        @Schema(description = "리뷰 섹션")
        private ReviewSection reviewSection;

        public static DetailWithReview fromEntity(Product product, ReviewSection reviewSection) {
            Integer discountedPrice = product.getOriginal_price() -
                    (product.getOriginal_price() * product.getDiscount_rate() / 100);

            return DetailWithReview.builder()
                    .productId(product.getId())
                    .category(product.getCategory())
                    .title(product.getTitle())
                    .subtitle(product.getSubtitle())
                    .mainImageUrl(product.getMain_image_url())
                    .detailImageUrl(product.getDetail_image_url())
                    .seller(product.getSeller())
                    .discountRate(product.getDiscount_rate())
                    .originalPrice(product.getOriginal_price())
                    .discountedPrice(discountedPrice)
                    .unit(product.getUnit())
                    .weightOrVolume(product.getWeight_or_volume())
                    .expirationDate(product.getExpiration_date())
                    .origin(product.getOrigin())
                    .packaging(product.getPackaging())
                    .shippingInfo(product.getShipping_info())
                    .notice(product.getNotice())
                    .description(product.getDescription())
                    .reviewSection(reviewSection)
                    .build();
        }
    }

    @Schema(description = "상품의 리뷰 섹션")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewSection {

        @Schema(description = "리뷰 통계")
        private org.example.be17pickcook.domain.review.model.ReviewDto.StatisticsResponse statistics;

        @Schema(description = "리뷰 목록 (기본 10개)")
        private List<org.example.be17pickcook.domain.review.model.ReviewDto.Response> recentReviews;

        @Schema(description = "내 리뷰 (있는 경우)")
        private org.example.be17pickcook.domain.review.model.ReviewDto.Response myReview;
    }

    // ================== 등록 DTO ==================

    @Schema(description = "상품 등록 요청 정보")
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Register {

        @Schema(description = "상품 카테고리 (필수)",
                example = "채소",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        private String category;

        @Schema(description = "상품 제목 (필수)",
                example = "신선한 유기농 상추 500g",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        private String title;

        @Schema(description = "상품 부제목 (선택사항)",
                example = "아침에 따온 신선한 유기농 상추입니다")
        private String subtitle;

        @Schema(description = "대표 이미지 URL (선택사항)",
                example = "https://example.com/product1.jpg")
        private String main_image_url;

        @Schema(description = "상세 이미지 URL (선택사항)",
                example = "https://example.com/product1_detail.jpg")
        private String detail_image_url;

        @Schema(description = "판매자 이름 (선택사항)",
                example = "김농부")
        private String seller;

        @Schema(description = "할인율 (%) (선택사항)",
                example = "15",
                minimum = "0",
                maximum = "99")
        private Integer discount_rate;

        @Schema(description = "정가 (원) (선택사항)",
                example = "5000",
                minimum = "0")
        private Integer original_price;

        @Schema(description = "판매 단위 (선택사항)",
                example = "1봉지")
        private String unit;

        @Schema(description = "무게/용량 (선택사항)",
                example = "500g")
        private String weight_or_volume;

        @Schema(description = "유통기한 (선택사항)",
                example = "2025-02-15")
        private String expiration_date;

        @Schema(description = "원산지 (선택사항)",
                example = "국내산")
        private String origin;

        @Schema(description = "포장 방법 (선택사항)",
                example = "비닐포장")
        private String packaging;

        @Schema(description = "배송 정보 (선택사항)",
                example = "냉장배송")
        private String shipping_info;

        @Schema(description = "주의사항 (선택사항)",
                example = "직사광선을 피해 보관하세요")
        private String notice;

        @Schema(description = "상품 설명 (선택사항)",
                example = "신선하고 맛있는 유기농 상추입니다.")
        private String description;

        public Product toEntity(User authUser) {
            return Product.builder()
                    .category(this.category)
                    .title(this.title)
                    .subtitle(this.subtitle)
                    .main_image_url(this.main_image_url)
                    .detail_image_url(this.detail_image_url)
                    .seller(this.seller)
                    .discount_rate(this.discount_rate)
                    .original_price(this.original_price)
                    .unit(this.unit)
                    .weight_or_volume(this.weight_or_volume)
                    .expiration_date(this.expiration_date)
                    .origin(this.origin)
                    .packaging(this.packaging)
                    .shipping_info(this.shipping_info)
                    .notice(this.notice)
                    .description(this.description)
                    .build();
        }
    }

    // ================== 수정 DTO ==================

    @Schema(description = "상품 수정 요청 정보 (모든 필드 선택사항)")
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Update {

        @Schema(description = "상품 제목 (선택사항)",
                example = "프리미엄 유기농 상추 500g")
        private String title;

        @Schema(description = "할인율 (%) (선택사항)",
                example = "20",
                minimum = "0",
                maximum = "99")
        private Integer discount_rate;

        @Schema(description = "정가 (원) (선택사항)",
                example = "6000",
                minimum = "0")
        private Integer original_price;

        @Schema(description = "대표 이미지 URL (선택사항)",
                example = "https://example.com/product1_new.jpg")
        private String main_image_url;

        @Schema(description = "상세 이미지 URL (선택사항)",
                example = "https://example.com/product1_detail_new.jpg")
        private String detail_image_url;

        @Schema(description = "판매 단위 (선택사항)",
                example = "2봉지")
        private String unit;

        @Schema(description = "무게/용량 (선택사항)",
                example = "750g")
        private String weight_or_volume;

        @Schema(description = "유통기한 (선택사항)",
                example = "2025-03-15")
        private String expiration_date;

        @Schema(description = "원산지 (선택사항)",
                example = "유기농 인증 국내산")
        private String origin;

        @Schema(description = "포장 방법 (선택사항)",
                example = "친환경 포장")
        private String packaging;

        @Schema(description = "배송 정보 (선택사항)",
                example = "당일배송 가능")
        private String shipping_info;

        @Schema(description = "주의사항 (선택사항)",
                example = "냉장고에 보관하세요")
        private String notice;

        @Schema(description = "상품 설명 (선택사항)",
                example = "더욱 신선하고 맛있는 프리미엄 유기농 상추")
        private String description;

        public void apply(Product entity) {
            if (title != null) entity.setTitle(title);
            if (discount_rate != null) entity.setDiscount_rate(discount_rate);
            if (original_price != null) entity.setOriginal_price(original_price);
            if (main_image_url != null) entity.setMain_image_url(main_image_url);
            if (detail_image_url != null) entity.setDetail_image_url(detail_image_url);
            if (unit != null) entity.setUnit(unit);
            if (weight_or_volume != null) entity.setWeight_or_volume(weight_or_volume);
            if (expiration_date != null) entity.setExpiration_date(expiration_date);
            if (origin != null) entity.setOrigin(origin);
            if (packaging != null) entity.setPackaging(packaging);
            if (shipping_info != null) entity.setShipping_info(shipping_info);
            if (notice != null) entity.setNotice(notice);
            if (description != null) entity.setDescription(description);
        }
    }
}
