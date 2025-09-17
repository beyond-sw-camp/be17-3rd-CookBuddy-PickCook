package org.example.be17pickcook.domain.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.be17pickcook.common.BaseEntity;
import org.example.be17pickcook.domain.recipe.model.RecipeNutrition;
import org.example.be17pickcook.domain.review.model.Review;
import org.example.be17pickcook.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Orders extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    @Column(unique = true, nullable = false)
    private String paymentId;
    private String orderNumber; // 고객에게 보여줄 주문 아이디
    private Integer total_price;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String paymentMethod;
    private LocalDateTime approvedAt; // 결제 완료 시간
    private String orderType; // 결제 요청 온 경로 (장바구니, 바로구매)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrderDelivery orderDelivery;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    public void addItems(OrderItem item) {
        if (this.orderItems == null) this.orderItems = new ArrayList<>();

        this.orderItems.add(item);
        item.setOrder(this);
    }

    public void addOrderDelivery(OrderDelivery orderDelivery) {
        this.orderDelivery =  orderDelivery;
        orderDelivery.setOrder(this);
    }

    public void updateOrderNumber(String newOrderNumber) {this.orderNumber = newOrderNumber;}
    public void updateStatus(OrderStatus newStatus) { this.status = newStatus; }
    public void updatePaymentMethod(String newPaymentMethod) {
        this.paymentMethod = newPaymentMethod;
    }
    public void updateApproveAt(LocalDateTime newApproveAt) { this.approvedAt = newApproveAt; }
}
