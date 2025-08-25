package org.example.be17pickcook.domain.product.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.be17pickcook.common.BaseEntity;
import org.example.be17pickcook.domain.review.model.Review;
import org.example.be17pickcook.domain.user.model.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    // 카테고리 (예: 채소, 과일, 정육 등) – 한글/영문/숫자/공백 허용
    @Pattern(
            regexp = "^[a-zA-Z가-힣0-9\\s]{1,30}$",
            message = "카테고리는 한글, 영어, 숫자, 공백만 입력 가능하며 최대 30자까지 허용됩니다."
    )
    @Column(name = "category", length = 30, nullable = false)
    private String category;

    // 상품 제목 – 한글/영문/숫자/공백 허용, 최대 50자
    @Pattern(
            regexp = "^[a-zA-Z가-힣0-9\\s]{1,50}$",
            message = "상품 제목은 한글, 영어, 숫자, 공백만 입력 가능하며 최대 50자까지 허용됩니다."
    )
    @Column(name = "title", length = 80, nullable = false)
    private String title;

    // 상품 부제목 – 최대 100자
    @Size(max = 100, message = "상품 부제목은 최대 100자까지 허용됩니다.")
    @Column(name = "subtitle", length = 100, nullable = false)
    private String subtitle;

    @Column(name = "main_image_url")
    private String main_image_url;

    @Column(name = "detail_image_url")
    private String detail_image_url;

    // 판매자 이름 – 한글/영문, 최대 30자
    @Pattern(
            regexp = "^[a-zA-Z가-힣\\s]{1,30}$",
            message = "판매자 이름은 한글과 영어만 입력 가능하며 최대 30자까지 허용됩니다."
    )
    @Column(name = "seller", length = 30, nullable = false)
    private String seller;

    // 할인율
    @Min(value = 0, message = "할인율은 0 이상이어야 합니다.")
    @Max(value = 99, message = "할인율은 99% 이하여야 합니다.")
    @Column(name = "discount_rate", nullable = false)
    private Integer discount_rate;

    // 정가
    @Min(value = 0, message = "정가는 0원 이상이어야 합니다.")
    @Column(name = "original_price", nullable = false)
    private Integer original_price;

    // 단위 – "1봉지", "2팩", "3개입" 같은 형태 허용
    @Pattern(
            regexp = "^[0-9]+[가-힣a-zA-Z]{1,10}$",
            message = "단위는 숫자와 한글/영문 단위를 조합해 입력해야 합니다. 예: 1봉지, 2개입, 3pack"
    )
    @Column(name = "unit", length = 20, nullable = false)
    private String unit;

    // 무게/용량 – 예: 500g, 2kg, 1L, 500ml 등
    @Pattern(
            regexp = "^[0-9]+(g|kg|ml|l|L|개입|봉지)?$",
            message = "무게/용량은 숫자와 단위를 조합해 입력해야 합니다. 예: 500g, 2kg, 1L"
    )
    @Column(name = "weight_or_volume", length = 50, nullable = false)
    private String weight_or_volume;

    @Column(name = "expiration_date")
    private String expiration_date;

    // 원산지 – 최대 50자
    @Size(max = 50, message = "원산지는 최대 50자까지 허용됩니다.")
    @Column(name = "origin", length = 50, nullable = false)
    private String origin;

    // 포장 타입 – 최대 50자
    @Size(max = 50, message = "포장 타입은 최대 500자까지 허용됩니다.")
    @Column(name = "packaging", length = 500, nullable = false)
    private String packaging;

    // 배송 안내 – 최대 200자
    @Size(max = 200, message = "배송 관련 안내는 최대 200자까지 허용됩니다.")
    @Column(name = "shipping_info", length = 200, nullable = false)
    private String shipping_info;

    // 소비자 안내문구 – 최대 300자
    @Size(max = 300, message = "소비자 안내문구는 최대 300자까지 허용됩니다.")
    @Column(name = "notice", length = 300, nullable = false)
    private String notice;

    // 상세 설명 – 최대 2000자
    @Size(max = 2000, message = "상세 설명은 최대 2000자까지 허용됩니다.")
    @Column(name = "description", length = 2000, nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();


    public void setMainImageUrl(String url) { this.main_image_url = url; }
    public void setDetailImageUrl(String url) { this.detail_image_url = url; }

    public void changeDiscountRate(Integer rate) {
        if (rate == null || rate < 0 || rate > 99) {
            throw new IllegalArgumentException("할인율은 0% 이상 99% 이하여야 합니다.");
        }
        this.discount_rate = rate;
    }
}
