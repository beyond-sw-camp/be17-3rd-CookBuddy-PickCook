package org.example.be17pickcook.domain.order.model;

import lombok.Builder;
import lombok.Getter;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.user.model.User;

import java.time.LocalDateTime;

public class RefundDto {
    @Getter
    @Builder
    public static class RefundRequestDto {
        private String paymentId;           // 결제 건 아이디
        private String reason;              // 취소 사유
        private Long amount;                // 취소 금액 (없으면 전액)
        private Long currentCancellableAmount; // 부분취소 시 잔액 검증용
        private Long orderId;
        private Long productId;


        public Refund toEntity(Integer userIdx) {
            User user = User.builder().idx(userIdx).build();
            Orders order = Orders.builder().idx(this.orderId).build();
            Product product = Product.builder().id(this.productId).build();

            return Refund.builder()
                    .order(order)
                    .user(user)
                    .product(product)
                    .reason(this.reason)
                    .amount(this.amount != null ? this.amount.intValue() : null)
                    .currentCancellableAmount(this.currentCancellableAmount != null ? this.currentCancellableAmount.intValue() : null)
                    .requestedAt(LocalDateTime.now())
                    .allRefund(this.amount == null) // 금액이 지정되지 않으면 전액
                    .status(RefundStatus.PENDING)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class RefundResponseDto {
        private String paymentId;
        private String status; // SUCCESS, FAILED
        private Long refundedAmount;

        public static RefundResponseDto fromEntity(Refund refund, String paymentId) {
            return RefundResponseDto.builder()
                    .paymentId(paymentId)
                    .status(refund.getStatus().name()) // Enum -> String
                    .refundedAmount(refund.getAmount() != null ? refund.getAmount().longValue() : 0L)
                    .build();
        }

        public static RefundResponseDto success(String paymentId, Integer refundedAmount) {
            return RefundResponseDto.builder()
                    .paymentId(paymentId)
                    .status(RefundStatus.SUCCESS.name())
                    .refundedAmount(refundedAmount != null ? refundedAmount.longValue() : 0L)
                    .build();
        }

        public static RefundResponseDto fail(String paymentId) {
            return RefundResponseDto.builder()
                    .paymentId(paymentId)
                    .status(RefundStatus.FAILED.name())
                    .refundedAmount(0L)
                    .build();
        }
    }
}
