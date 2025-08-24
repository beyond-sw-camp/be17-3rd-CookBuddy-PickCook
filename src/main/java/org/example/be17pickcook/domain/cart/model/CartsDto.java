package org.example.be17pickcook.domain.cart.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CartsDto {
    @Getter
    @Builder
    @Schema(description = "장바구니 등록 요청 DTO")
    public static class CartsRequestDto {
        @Schema(description = "상품 수량", example = "3")
        private Integer quantity;
        @Schema(description = "상품 고유 ID", example = "[3, 5, 7]")
        private List<Long> product_ids;

        // Dto -> Entity 변환 메서드
        public List<Carts> toEntity(User authUser) {
            if (product_ids == null || product_ids.isEmpty()) return Collections.emptyList();

            return product_ids.stream()
                    .map(productId -> Carts.builder()
                            .quantity(quantity != null ? quantity : 1)
                            .product(Product.builder().id(productId).build())
                            .user(authUser)
                            .build())
                    .collect(Collectors.toList());
        }
    }


    @Getter
    @Builder
    @Schema(description = "장바구니 응답 DTO")
    public static class CartsResponseDto {
        @Schema(description = "장바구니 상품 ID", example = "1")
        private Long idx;
        @Schema(description = "상품 자체 ID", example = "1")
        private Long product_id;
        @Schema(description = "상품 이름", example = "1")
        private String name;
        @Schema(description = "상품 이미지", example = "url경로")
        private String main_image_url;
        @Schema(description = "할인율", example = "1")
        private Integer discount_rate;
        @Schema(description = "상품 가격", example = "1")
        private Integer original_price;
        @Schema(description = "상품 수량", example = "1")
        private Integer quantity;

        public static CartsResponseDto fromEntity(Carts carts) {
            return CartsResponseDto.builder()
                    .idx(carts.getIdx())
                    .product_id(carts.getProduct() != null ? carts.getProduct().getId() : null)
                    .name(carts.getProduct() != null ? carts.getProduct().getTitle() : null)
                    .main_image_url(carts.getProduct() != null ? carts.getProduct().getMain_image_url() : null)
                    .discount_rate(carts.getProduct() != null ? carts.getProduct().getDiscount_rate() : null)
                    .original_price(carts.getProduct() != null ? carts.getProduct().getOriginal_price() : null)
                    .quantity(carts.getQuantity())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CartQuantityUpdateRequest {
        private Integer quantity;
    }
}
