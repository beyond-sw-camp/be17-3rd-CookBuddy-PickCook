package org.example.be17pickcook.product.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Product DTOs (엔티티 변환을 DTO 내부에서 처리: from / toEntity / apply)
 */
public class ProductDto {

    // ================== 응답 DTO ==================
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Res {
        private Long id;
        private String category;
        private String title;
        private String subtitle;
        private String mainImageUrl;
        private String detailImageUrl;
        private String productUrl;
        private String seller;
        private Integer price;
        private Integer discountRate;
        private Integer originalPrice;
        private String unit;
        private String weightOrVolume;
        private String expirationDate; // DB가 varchar -> String 유지
        private String origin;
        private String packaging;
        private String shippingInfo;
        private String notice;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Res from(Product entity) {
            return Res.builder()
                    .id(entity.getId())
                    .category(entity.getCategory())
                    .title(entity.getTitle())
                    .subtitle(entity.getSubtitle())
                    .mainImageUrl(entity.getMainImageUrl())
                    .detailImageUrl(entity.getDetailImageUrl())
                    .productUrl(entity.getProductUrl())
                    .seller(entity.getSeller())
                    .price(entity.getPrice())
                    .discountRate(entity.getDiscountRate())
                    .originalPrice(entity.getOriginalPrice())
                    .unit(entity.getUnit())
                    .weightOrVolume(entity.getWeightOrVolume())
                    .expirationDate(entity.getExpirationDate())
                    .origin(entity.getOrigin())
                    .packaging(entity.getPackaging())
                    .shippingInfo(entity.getShippingInfo())
                    .notice(entity.getNotice())
                    .description(entity.getDescription())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        }
    }

    // ================== 등록 DTO ==================
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Register {
        @NotBlank private String category;
        @NotBlank private String title;

        private String subtitle;
        private String mainImageUrl;
        private String detailImageUrl;
        private String productUrl;
        private String seller;

        @NotNull private Integer price;
        private Integer discountRate;
        private Integer originalPrice;

        private String unit;
        private String weightOrVolume;
        private String expirationDate;
        private String origin;
        private String packaging;
        private String shippingInfo;
        private String notice;
        private String description;

        public Product toEntity() {
            return Product.builder()
                    .category(category)
                    .title(title)
                    .subtitle(subtitle)
                    .mainImageUrl(mainImageUrl)
                    .detailImageUrl(detailImageUrl)
                    .productUrl(productUrl)
                    .seller(seller)
                    .price(price)
                    .discountRate(discountRate)
                    .originalPrice(originalPrice)
                    .unit(unit)
                    .weightOrVolume(weightOrVolume)
                    .expirationDate(expirationDate)
                    .origin(origin)
                    .packaging(packaging)
                    .shippingInfo(shippingInfo)
                    .notice(notice)
                    .description(description)
                    .build();
        }
    }

    // ================== 수정 DTO ==================
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Update {
        private String title;
        private Integer price;
        private Integer discountRate;
        private Integer originalPrice;
        private String mainImageUrl;
        private String detailImageUrl;
        private String productUrl;
        private String unit;
        private String weightOrVolume;
        private String expirationDate;
        private String origin;
        private String packaging;
        private String shippingInfo;
        private String notice;
        private String description;

        public void apply(Product entity) {
            if (title != null) entity.setTitle(title);
            if (price != null) entity.setPrice(price);
            if (discountRate != null) entity.setDiscountRate(discountRate);
            if (originalPrice != null) entity.setOriginalPrice(originalPrice);
            if (mainImageUrl != null) entity.setMainImageUrl(mainImageUrl);
            if (detailImageUrl != null) entity.setDetailImageUrl(detailImageUrl);
            if (productUrl != null) entity.setProductUrl(productUrl);
            if (unit != null) entity.setUnit(unit);
            if (weightOrVolume != null) entity.setWeightOrVolume(weightOrVolume);
            if (expirationDate != null) entity.setExpirationDate(expirationDate);
            if (origin != null) entity.setOrigin(origin);
            if (packaging != null) entity.setPackaging(packaging);
            if (shippingInfo != null) entity.setShippingInfo(shippingInfo);
            if (notice != null) entity.setNotice(notice);
            if (description != null) entity.setDescription(description);
        }
    }
}
