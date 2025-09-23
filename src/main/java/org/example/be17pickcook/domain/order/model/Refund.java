package org.example.be17pickcook.domain.order.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.be17pickcook.common.BaseEntity;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.user.model.User;

import java.time.LocalDateTime;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Refund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long idx;

    private String reason;
    private Integer amount;
    private Integer currentCancellableAmount; // 부분취소 시 잔액 검증용

    @Enumerated(EnumType.STRING)
    private RefundStatus status; // SUCCESS, FAILED

    private LocalDateTime requestedAt;
    private Boolean allRefund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Orders order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public void setStatus(RefundStatus refundStatus) {
        this.status = refundStatus;
    }

    public void setAmount(Integer refundedAmount) {
        this.amount = refundedAmount;
    }
}
