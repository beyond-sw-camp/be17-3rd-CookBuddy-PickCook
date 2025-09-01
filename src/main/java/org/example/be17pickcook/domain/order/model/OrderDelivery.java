package org.example.be17pickcook.domain.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class OrderDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    private String receiverName;
    private String receiverPhone;
    private Integer zipCode;
    private String address;
    private String detailAddress;
    private String deliveryPlace;
    private String requestMessage;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Orders order;

    public void setOrder(Orders order) { this.order = order; }
}
