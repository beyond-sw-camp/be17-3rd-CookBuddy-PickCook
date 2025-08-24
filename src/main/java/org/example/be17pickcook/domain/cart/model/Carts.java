package org.example.be17pickcook.domain.cart.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.be17pickcook.common.BaseEntity;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.user.model.User;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Carts extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
