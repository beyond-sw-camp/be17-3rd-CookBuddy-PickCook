package org.example.be17pickcook.domain.product.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.be17pickcook.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

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
        private String main_image_url;
        private String detail_image_url;
        private String seller;
        private Integer discount_rate;
        private Integer original_price;
        private String unit;
        private String weight_or_volume;
        private String expiration_date; // DB가 varchar -> String 유지
        private String origin;
        private String packaging;
        private String shipping_info;
        private String notice;
        private String description;
        private LocalDateTime created_at;
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

    // ================== 등록 DTO ==================
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Register {
        @NotBlank private String category;
        @NotBlank private String title;

        private String subtitle;
        private String main_image_url;
        private String detail_image_url;
        private String seller;

        private Integer discount_rate;
        private Integer original_price;

        private String unit;
        private String weight_or_volume;
        private String expiration_date;
        private String origin;
        private String packaging;
        private String shipping_info;
        private String notice;
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
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Update {
        private String title;
        private Integer discount_rate;
        private Integer original_price;
        private String main_image_url;
        private String detail_image_url;
        private String unit;
        private String weight_or_volume;
        private String expiration_date;
        private String origin;
        private String packaging;
        private String shipping_info;
        private String notice;
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
