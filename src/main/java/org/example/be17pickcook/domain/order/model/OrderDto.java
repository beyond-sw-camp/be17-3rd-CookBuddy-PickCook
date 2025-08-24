package org.example.be17pickcook.domain.order.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.checkerframework.checker.units.qual.A;
import org.example.be17pickcook.domain.cart.model.Carts;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.user.model.User;

import java.util.List;

public class OrderDto {

    @Getter
    @Builder
    @Schema(description = "결제 시작 요청 DTO")
    public static class PaymentStartReqDto {
        private Integer total_price;
        private List<OrderItemDto> orderItems;

        public Orders toEntity(User authUser, String paymentId) {
            Orders order = Orders.builder()
                    .total_price(this.total_price)
                    .paymentId(paymentId)
                    .status(OrderStatus.PENDING)
                    .user(authUser)
                    .build();

            if (orderItems != null) {
                for (OrderDto.OrderItemDto item : orderItems) {
                    OrderItem itemEntity = item.toEntity(order);
                    order.addItems(itemEntity);
                }
            }

            return order;
        }
    }


    @Getter
    @Builder
    @Schema(description = "결제 상품 DTO")
    public static class OrderItemDto {
        private Long product_id;
        private Long cart_id;
        private String product_name;
        private Integer product_price;
        private Integer quantity;

        public OrderItem toEntity(Orders order) {
            return OrderItem.builder()
                    .quantity(quantity)
                    .product_name(product_name)
                    .product_price(product_price)
                    .order(order)
                    .product(Product.builder().id(product_id).build())
                    .cart(Carts.builder().idx(cart_id).build())
                    .build();
        }

        public static OrderItemDto fromEntity(OrderItem orderitem) {
            return OrderItemDto.builder()
                    .product_id(orderitem.getProduct().getId())
                    .cart_id(orderitem.getCart().getIdx())
                    .product_name(orderitem.getProduct_name())
                    .product_price(orderitem.getProduct_price())
                    .quantity(orderitem.getQuantity())
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class PaymentStartResDto {
        private String paymentId;
        private String status;
    }

    @Getter
    @AllArgsConstructor
    public static class PaymentValidationReqDto {
        private String paymentId;
    }

    @Getter
    @AllArgsConstructor
    public static class PaymentValidationResDto {
        private Long order_id;
        private String status;
    }

}
