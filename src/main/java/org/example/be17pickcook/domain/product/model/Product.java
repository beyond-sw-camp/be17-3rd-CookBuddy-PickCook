package org.example.be17pickcook.domain.product.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.be17pickcook.domain.user.model.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

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

    @Pattern(regexp = "^[a-zA-Z가-힣]{1,30}$",
            message = "카테고리는 영어와 한글만 입력 가능하며 최대 30자까지 허용됩니다.")
    @Column(name = "category", length = 30, nullable = false)
    private String category;

    @Pattern(regexp = "^[a-zA-Z가-힣]{1,10}$",
            message = "상품 제목은 영어와 한글만 가능하며 최대 10자까지 허용됩니다.")
    @Column(name = "title", length = 10, nullable = false)
    private String title;

    @Column(name = "subtitle", length = 50, nullable = false)
    @Size(max = 50, message = "상품 부제목은 최대 50자까지 허용됩니다.")
    private String subtitle;

    @Column(name = "main_image_url")
    private String mainImageUrl;

    @Column(name = "detail_image_url")
    private String detailImageUrl;

    @Pattern(regexp = "^[a-zA-Z가-힣]{1,10}$",
            message = "판매자 이름은 영어와 한글만 입력 가능하며 최대 10자까지 허용됩니다.")
    @Column(name = "seller", length = 10, nullable = false)
    private String seller;

    @Pattern(regexp = "^[가-힣\\s]{2,30}$",
            message = "판매자 주소는 한글로만 작성해야 하며, 시/구 단위까지만 입력 가능합니다.")
    @Column(name = "seller_address", length = 30, nullable = false)
    private String seller_address;

    @Min(value = 0, message = "판매가는 0원 이상이어야 합니다.")
    @Column(name = "price", nullable = false)
    private Integer price;

    @Min(value = 0, message = "할인율은 0 이상이어야 합니다.")
    @Max(value = 99, message = "할인율은 99% 이하여야 합니다.")
    @Column(name = "discount_rate", nullable = false)
    private Integer discountRate;

    @Min(value = 0, message = "정가는 0원 이상이어야 합니다.")
    @Column(name = "original_price", nullable = false)
    private Integer originalPrice;

    @Pattern(regexp = "^[0-9]+[가-힣]{1,5}$",
            message = "단위는 숫자와 한글 단위를 조합해 입력해야 합니다. 예: 1봉, 2개, 3박스")
    @Column(name = "unit", length = 10, nullable = false)
    private String unit;

    @Pattern(regexp = "^[0-9]+(g|kg|ml|l|L)$",
            message = "무게/용량은 숫자와 단위를 조합해 입력해야 합니다. 예: 500g, 2kg, 1L")
    @Column(name = "weight_or_volume", length = 10, nullable = false)
    private String weightOrVolume;

    @Column(name = "expiration_date")
    private String expirationDate;

    @Size(max = 10, message = "원산지는 최대 10자까지 허용됩니다.")
    @Column(name = "origin", length = 10, nullable = false)
    private String origin;

    @Size(max = 10, message = "포장 타입은 최대 10자까지 허용됩니다.")
    @Column(name = "packaging", length = 10, nullable = false)
    private String packaging;

    @Size(max = 100, message = "배송 관련 안내는 최대 100자까지 허용됩니다.")
    @Column(name = "shipping_info", length = 100, nullable = false)
    private String shippingInfo;

    @Size(max = 200, message = "소비자 안내문구는 최대 200자까지 허용됩니다.")
    @Column(name = "notice", length = 200, nullable = false)
    private String notice;

    @Size(max = 1000, message = "상세 설명은 최대 1000자까지 허용됩니다.")
    @Column(name = "description", length = 1000, nullable = false)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 편의 메서드
    public void changePrice(Integer price) {
        if (price == null || price < 0) throw new IllegalArgumentException("판매가는 0원 이상이어야 합니다.");
        this.price = price;
    }
    public void setMainImageUrl(String url) { this.mainImageUrl = url; }
    public void setDetailImageUrl(String url) { this.detailImageUrl = url; }

    public void changeDiscountRate(Integer rate) {
        if (rate == null || rate < 0 || rate > 99) throw new IllegalArgumentException("할인율은 0% 이상 99% 이하여야 합니다.");
        this.discountRate = rate;
    }
}
