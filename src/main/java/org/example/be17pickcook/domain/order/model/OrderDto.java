package org.example.be17pickcook.domain.order.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.checkerframework.checker.units.qual.A;
import org.example.be17pickcook.domain.cart.model.Carts;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderDto {

    @Getter
    @Builder
    @Schema(description = "결제 시작 요청 DTO")
    public static class PaymentStartReqDto {
        private Integer total_price;
        private String orderType;
        private List<OrderItemDto> orderItems;

        public Orders toEntity(User authUser, String paymentId) {
            Orders order = Orders.builder()
                    .total_price(this.total_price)
                    .orderType(this.orderType)
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
                    .build();
        }

        public static OrderItemDto fromEntity(OrderItem orderitem) {
            return OrderItemDto.builder()
                    .product_id(orderitem.getProduct().getId())
                    .product_name(orderitem.getProduct_name())
                    .product_price(orderitem.getProduct_price())
                    .quantity(orderitem.getQuantity())
                    .build();
        }
    }

    // 주문내역 리스트에서 상품 단일 컴포넌트에 필요한 데이터들
    @Getter
    @Builder
    public static class OrderInfoDto {
        private Long product_id;
        private String product_name;
        private Integer product_price;
        private Integer quantity;
        private String product_image;
        private String product_amount;
        private String status;

        public static OrderInfoDto fromEntity(OrderItem orderitem) {
            OrderItemDto itemDto = OrderItemDto.fromEntity(orderitem);

            return OrderInfoDto.builder()
                    .product_id(itemDto.getProduct_id())
                    .product_name(itemDto.getProduct_name())
                    .product_price(itemDto.getProduct_price())
                    .quantity(itemDto.getQuantity())
                    .product_image(orderitem.getProduct().getMain_image_url())
                    .product_amount(orderitem.getProduct().getWeight_or_volume())
                    .status(orderitem.getOrder().getStatus().name())
                    .build();
        }
    }


    // 주문내역 리스트에서 한 날짜/시간에 포함되는 주문내역에 들어가는 데이터들
    @Getter
    @Builder
    public static class DayOrderInfoDto {
        private Long order_id;
        private LocalDateTime date;
        private List<OrderItemDto> orderItems;
    }


    // 주문내역 리스트에 필요한 데이터들
    @Getter
    @Builder
    public static class OrderInfoListDto {
        private List<DayOrderInfoDto> orderItems;

        public static OrderInfoListDto fromEntities(List<OrderItem> orderItems) {
            // order_id별로 그룹핑
            Map<Long, List<OrderItem>> groupedByOrder = orderItems.stream()
                    .collect(Collectors.groupingBy(item -> item.getOrder().getIdx()));

            List<DayOrderInfoDto> dayOrderDtos = groupedByOrder.entrySet().stream()
                    .map(entry -> {
                        List<OrderItem> items = entry.getValue();
                        Orders order = items.get(0).getOrder(); // 같은 order_id이므로 첫 번째로 대표
                        List<OrderItemDto> itemDtos = items.stream()
                                .map(OrderItemDto::fromEntity) // 기존 fromEntity 재사용
                                .collect(Collectors.toList());

                        return DayOrderInfoDto.builder()
                                .order_id(order.getIdx())
                                .date(order.getCreatedAt())
                                .orderItems(itemDtos)
                                .build();
                    })
                    .collect(Collectors.toList());

            return OrderInfoListDto.builder()
                    .orderItems(dayOrderDtos)
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
