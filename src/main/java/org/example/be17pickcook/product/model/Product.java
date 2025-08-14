package org.example.be17pickcook.product.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "category")
    private String category;

    @Column(name = "title")
    private String title;

    @Column(name = "subtitle", columnDefinition = "text")
    private String subtitle;

    @Column(name = "main_image_url")
    private String mainImageUrl;

    @Column(name = "detail_image_url")
    private String detailImageUrl;

    @Column(name = "product_url")
    private String productUrl;

    @Column(name = "seller")
    private String seller;

    @Column(name = "price")
    private Integer price;

    @Column(name = "discount_rate")
    private Integer discountRate;

    @Column(name = "original_price")
    private Integer originalPrice;

    @Column(name = "unit")
    private String unit;

    @Column(name = "weight_or_volume")
    private String weightOrVolume;

    // 스키마가 varchar이므로 String 유지 (DTO도 String)
    @Column(name = "expiration_date")
    private String expirationDate;

    @Column(name = "origin")
    private String origin;

    @Column(name = "packaging")
    private String packaging;

    @Column(name = "shipping_info", columnDefinition = "text")
    private String shippingInfo;

    @Column(name = "notice", columnDefinition = "text")
    private String notice;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // (옵션) 부분 수정 편의 메서드
    public void changePrice(Integer price) { this.price = price; }
    public void changeDiscountRate(Integer rate) { this.discountRate = rate; }
}
