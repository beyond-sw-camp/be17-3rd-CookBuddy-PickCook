package org.example.be17pickcook.domain.order.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문 관련 DTO 클래스들")
public class OrderDto {

    @Getter
    @Builder
    @Schema(description = "결제 시작 요청 DTO")
    public static class PaymentStartReqDto {
        @Schema(description = "총 결제 금액", example = "25000")
        private Integer total_price;

        @Schema(description = "주문 타입", example = "ONLINE")
        private String orderType;

        @Schema(description = "주문 상품 목록")
        private List<OrderItemDto> orderItems;

        @Schema(description = "배송 정보")
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
    @Schema(description = "주문 상품 DTO")
    public static class OrderItemDto {
        @Schema(description = "상품 ID", example = "1")
        private Long product_id;

        @Schema(description = "장바구니 ID", example = "10")
        private Long cart_id;

        @Schema(description = "상품명", example = "신선한 유기농 상추")
        private String product_name;

        @Schema(description = "상품 가격", example = "5000")
        private Integer product_price;

        @Schema(description = "주문 수량", example = "2")
        private Integer quantity;

        @Schema(description = "배송 상태", example = "READY")
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
    @Schema(description = "배송 정보 DTO")
    public static class OrderDeliveryDto {
        @Schema(description = "받는 사람 이름", example = "홍길동")
        private String receiverName;

        @Schema(description = "받는 사람 전화번호", example = "010-1234-5678")
        private String receiverPhone;

        @Schema(description = "우편번호", example = "12345")
        private Integer zipCode;

        @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
        private String address;

        @Schema(description = "상세 주소", example = "456호")
        private String detailAddress;

        @Schema(description = "배송 장소", example = "문 앞")
        private String deliveryPlace;

        @Schema(description = "배송 요청사항", example = "부재 시 문 앞에 놓아주세요")
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
    @Schema(description = "주문 상품 정보 DTO")
    public static class OrderInfoDto {
        @Schema(description = "상품 ID", example = "1")
        private Long product_id;

        @Schema(description = "상품명", example = "신선한 유기농 상추")
        private String product_name;

        @Schema(description = "원가", example = "5000")
        private Integer original_price;

        @Schema(description = "할인율", example = "15")
        private Integer discount_rate;

        @Schema(description = "주문 수량", example = "2")
        private Integer quantity;

        @Schema(description = "상품 이미지 URL", example = "https://example.com/product.jpg")
        private String product_image;

        @Schema(description = "상품 용량/무게", example = "500g")
        private String product_amount;

        @Schema(description = "배송 상태", example = "READY")
        private String status;

        @Schema(description = "리뷰 작성 여부", example = "true")
        private boolean hasReview;

        // 리뷰 작성 여부까지 조회용
        public static OrderInfoDto fromEntity(OrderItem orderItem, boolean hasReview) {
            return OrderInfoDto.builder()
                    .product_id(orderItem.getProduct().getId())
                    .product_name(orderItem.getProduct().getTitle())
                    .original_price(orderItem.getProduct().getOriginal_price())
                    .discount_rate(orderItem.getProduct().getDiscount_rate())
                    .quantity(orderItem.getQuantity())
                    .product_image(orderItem.getProduct().getMain_image_url())
                    .product_amount(orderItem.getProduct().getWeight_or_volume())
                    .status(orderItem.getDeliveryStatus().name())
                    .hasReview(hasReview)
                    .build();
        }

        // 리뷰 작성 여부 포함X  조회용
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
    @Schema(description = "주문 내역 목록 DTO")
    public static class OrderInfoListDto {
        @Schema(description = "주문 ID", example = "1")
        private Long orderId;

        @Schema(description = "주문 번호(고객용)", example = "1")
        private Long orderNumber;

        @Schema(description = "주문 날짜", example = "2025-01-15T14:30:00")
        private LocalDateTime date;

        @Schema(description = "주문 상품 목록")
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
    @Schema(description = "주문 상세 정보 DTO")
    public static class OrderDetailDto {
        @Schema(description = "주문 번호", example = "ORD20250115-12345")
        private String orderNumber;

        @Schema(description = "총 결제 금액", example = "25000")
        private Integer total_price;

        @Schema(description = "결제 승인 시간", example = "2025-01-15T14:30:00")
        private LocalDateTime approvedAt;

        @Schema(description = "결제 방법", example = "카드")
        private String paymentMethod;

        @Schema(description = "배송 정보")
        private OrderDeliveryDto orderDelivery; // 배송 정보

        @Schema(description = "주문 상품 목록")
        private List<OrderInfoDto> orderItems; // 상품 목록

        public static OrderDetailDto fromEntity(Orders order, List<OrderInfoDto> orderItems) {
            return OrderDetailDto.builder()
                    .orderNumber(order.getOrderNumber())
                    .total_price(order.getTotal_price())
                    .approvedAt(order.getApprovedAt())
                    .paymentMethod(order.getPaymentMethod())
                    .orderDelivery(OrderDeliveryDto.fromEntity(order.getOrderDelivery()))
                    .orderItems(orderItems)
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "결제 시작 응답 DTO")
    public static class PaymentStartResDto {
        @Schema(description = "결제 ID", example = "payment_123456")
        private String paymentId;

        @Schema(description = "결제 상태", example = "PENDING")
        private String status;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "결제 검증 요청 DTO")
    public static class PaymentValidationReqDto {
        @Schema(description = "결제 ID", example = "payment_123456")
        private String paymentId;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "결제 검증 응답 DTO")
    public static class PaymentValidationResDto {
        @Schema(description = "주문 ID", example = "1")
        private Long order_id;

        @Schema(description = "검증 상태", example = "SUCCESS")
        private String status;
    }
}