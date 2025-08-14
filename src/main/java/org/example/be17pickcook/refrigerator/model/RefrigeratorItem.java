package org.example.be17pickcook.refrigerator.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refrigerator_items")
public class RefrigeratorItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refrigerator_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refrigerator_id", nullable = false)
    private Refrigerator refrigerator;

    @Column(name = "ingredient_name", length = 255)
    private String ingredientName;

    @Column(length = 50)
    private String quantity;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // RefrigeratorItem.java

    public void changeQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void changeExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

}
