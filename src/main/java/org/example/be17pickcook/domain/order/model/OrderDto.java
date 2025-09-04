package org.example.be17pickcook.domain.order.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    @Getter
    @Builder
    @Schema(description = "결제 시작 요청 DTO")
    public static class PaymentStartReqDto {
        private Integer total_price;
        private String orderType;
        private List<OrderItemDto> orderItems;
        private OrderDeliveryDto orderDelivery;

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

            if (orderDelivery != null) {
                OrderDelivery orderDeliveryEntity = orderDelivery.toEntity(order);
                order.addOrderDelivery(orderDeliveryEntity);
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
        private DeliveryStatus deliveryStatus;

        public OrderItem toEntity(Orders order) {
            return OrderItem.builder()
                    .quantity(quantity)
                    .product_name(product_name)
                    .product_price(product_price)
                    .deliveryStatus(DeliveryStatus.READY)
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


    @Getter
    @Builder
    public static class OrderDeliveryDto {
        private String receiverName;
        private String receiverPhone;
        private Integer zipCode;
        private String address;
        private String detailAddress;
        private String deliveryPlace;
        private String requestMessage;

        public OrderDelivery toEntity(Orders order) {
            return OrderDelivery.builder()
                    .receiverName(receiverName)
                    .receiverPhone(receiverPhone)
                    .zipCode(zipCode)
                    .address(address)
                    .detailAddress(detailAddress)
                    .deliveryPlace(deliveryPlace)
                    .requestMessage(requestMessage)
                    .order(order)
                    .build();
        }

        public static OrderDeliveryDto fromEntity(OrderDelivery orderdelivery) {
            return OrderDeliveryDto.builder()
                    .receiverName(orderdelivery.getReceiverName())
                    .receiverPhone(orderdelivery.getReceiverPhone())
                    .zipCode(orderdelivery.getZipCode())
                    .address(orderdelivery.getAddress())
                    .detailAddress(orderdelivery.getDetailAddress())
                    .deliveryPlace(orderdelivery.getDeliveryPlace())
                    .requestMessage(orderdelivery.getRequestMessage())
                    .build();
        }
    }


    // 주문 내역 조회용
    @Getter
    @Builder
    public static class OrderInfoDto {
        private Long product_id;
        private String product_name;
        private Integer original_price;
        private Integer discount_rate;
        private Integer quantity;
        private String product_image;
        private String product_amount;
        private String status;

        public static OrderInfoDto fromEntity(OrderItem orderItem) {
            return OrderInfoDto.builder()
                    .product_id(orderItem.getProduct().getId())
                    .product_name(orderItem.getProduct().getTitle())
                    .original_price(orderItem.getProduct().getOriginal_price())
                    .discount_rate(orderItem.getProduct().getDiscount_rate())
                    .quantity(orderItem.getQuantity())
                    .product_image(orderItem.getProduct().getMain_image_url())
                    .product_amount(orderItem.getProduct().getWeight_or_volume())
                    .status(orderItem.getDeliveryStatus().name())
                    .build();
        }
    }


    // 주문 내역 목록 조회
    @Getter
    @Builder
    public static class OrderInfoListDto {
        private Long orderId;
        private LocalDateTime date;
        private List<OrderInfoDto> orderItems; // 기존 OrderInfoDto 사용

        public static OrderInfoListDto fromEntity(Orders order) {
            return OrderInfoListDto.builder()
                    .orderId(order.getIdx())
                    .date(order.getCreatedAt())
                    .orderItems(order.getOrderItems().stream()
                            .map(OrderInfoDto::fromEntity) // 여기서 OrderInfoDto 사용
                            .toList())
                    .build();
        }
    }


    @Getter
    @Builder
    public static class OrderDetailDto {
        private String orderNumber;
        private Integer total_price;
        private LocalDateTime approvedAt;
        private String paymentMethod;

        private OrderDeliveryDto orderDelivery; // 배송 정보
        private List<OrderInfoDto> orderItems; // 상품 목록

        public static OrderDetailDto fromEntity(Orders order) {
            return OrderDetailDto.builder()
                    .orderNumber(order.getOrderNumber())
                    .total_price(order.getTotal_price())
                    .approvedAt(order.getApprovedAt())
                    .paymentMethod(order.getPaymentMethod())
                    .orderDelivery(OrderDeliveryDto.fromEntity(order.getOrderDelivery()))
                    .orderItems(order.getOrderItems().stream()
                            .map(OrderInfoDto::fromEntity)
                            .toList()
                    )
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
