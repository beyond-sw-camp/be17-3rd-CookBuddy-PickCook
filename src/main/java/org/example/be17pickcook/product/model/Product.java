package org.example.be17pickcook.product.model;

import jakarta.persistence.*;
import lombok.*;
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
@Entity // JPA 엔티티: 이 클래스가 DB 테이블과 매핑됨
@Table(name = "products") // 실제 테이블 이름을 "products"로 지정
public class Product {

    @Id // 기본 키(PK) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT (DB에서 자동 증가)
    @Column(name = "product_id") // DB 컬럼명 = product_id
    private Long id;

    @Pattern(
            regexp = "^[a-zA-Z가-힣]{1,30}$",
            message = "카테고리는 영어와 한글만 입력 가능하며 최대 30자까지 허용됩니다."
    )
    @Column(name = "category", length = 30, nullable = false) // 상품 카테고리 (예: 과일, 정육, 유제품 등)
    private String category;

    @Pattern(
            regexp = "^[a-zA-Z가-힣]{1,10}$",
            message = "상품 제목은 영어와 한글만 가능하며 최대 10자까지 허용됩니다."
    )
    @Column(name = "title", length = 10, nullable = false)   // 상품 제목
    private String title;

    @Column(name = "subtitle", length = 50, nullable = false)
    @Size(max = 50, message = "상품 부제목은 최대 50자까지 허용됩니다.")// 상품 부제목
    private String subtitle;

    @Column(name = "main_image_url") // 대표 이미지 URL
    private String mainImageUrl;

    @Column(name = "detail_image_url") // 상세 이미지 URL
    private String detailImageUrl;

    @Column(name = "product_url") // 상품 상세 페이지 URL
    private String productUrl;

    // 판매자 이름
    @Pattern(
            regexp = "^[a-zA-Z가-힣]{1,10}$",
            message = "판매자 이름은 영어와 한글만 입력 가능하며 최대 10자까지 허용됩니다."
    )
    @Column(name = "seller", length = 10, nullable = false)
    private String seller;

    // 판매자 주소 - 예: "서울특별시 강남구", "부산광역시 해운대구"
    @Pattern(
            regexp = "^[가-힣\\s]{2,30}$",
            message = "판매자 주소는 한글로만 작성해야 하며, 시/구 단위까지만 입력 가능합니다."
    )
    @Column(name = "seller_address", length = 30, nullable = false)
    private String sellerAddress;


    // 실제 판매가 - 숫자만 허용 - 0 이상 필수
    @Min(value = 0, message = "판매가는 0원 이상이어야 합니다.")
    @Column(name = "price", nullable = false)
    private Integer price;

    // 할인율 (%) - 0 이상 99 이하
    @Min(value = 0, message = "할인율은 0 이상이어야 합니다.")
    @Max(value = 99, message = "할인율은 99% 이하여야 합니다.")
    @Column(name = "discount_rate", nullable = false)
    private Integer discountRate;

    // 정가 (할인 전 가격) - 숫자만 허용 - 0 이상 필수
    @Min(value = 0, message = "정가는 0원 이상이어야 합니다.")
    @Column(name = "original_price", nullable = false)
    private Integer originalPrice;


    // 단위 (예: 1봉, 2봉, 3개 등) - 숫자 1자리 이상 + 한글(개, 봉, 박스 등)
    @Pattern(
            regexp = "^[0-9]+[가-힣]{1,5}$",
            message = "단위는 숫자와 한글 단위를 조합해 입력해야 합니다. 예: 1봉, 2개, 3박스"
    )
    @Column(name = "unit", length = 10, nullable = false)
    private String unit;

    // 무게 또는 용량 (예: 500g, 2kg, 1L) - 숫자 1자리 이상 + 단위(kg, g, L 등) - 최대 10자 제한
    @Pattern(
            regexp = "^[0-9]+(g|kg|ml|l|L)$",
            message = "무게/용량은 숫자와 단위를 조합해 입력해야 합니다. 예: 500g, 2kg, 1L"
    )
    @Column(name = "weight_or_volume", length = 10, nullable = false)
    private String weightOrVolume;


    // 스키마가 VARCHAR라서 String 사용 (DB에서 DATE로 안 했으니 문자열로 저장됨)
    @Column(name = "expiration_date") // 유통기한
    private String expirationDate;

    // 원산지 (예: 국내산, 미국산 등)- 최대 10자 이내
    @Size(max = 10, message = "원산지는 최대 10자까지 허용됩니다.")
    @Column(name = "origin", length = 10, nullable = false)
    private String origin;

    // 포장 타입 (예: 진공포장, 벌크 등)- 최대 10자 이내
    @Size(max = 10, message = "포장 타입은 최대 10자까지 허용됩니다.")
    @Column(name = "packaging", length = 10, nullable = false)
    private String packaging;

    // 배송 관련 안내 (긴 문구 가능)- 최대 100자 이내
    @Size(max = 100, message = "배송 관련 안내는 최대 100자까지 허용됩니다.")
    @Column(name = "shipping_info", length = 100, nullable = false)
    private String shippingInfo;


    // 소비자 안내문구 (예: 알레르기 유발 정보) - 최대 200자 이내
    @Size(max = 200, message = "소비자 안내문구는 최대 200자까지 허용됩니다.")
    @Column(name = "notice", length = 200, nullable = false)
    private String notice;

    // 상세 설명 - 최대 1000자 이내
    @Size(max = 1000, message = "상세 설명은 최대 1000자까지 허용됩니다.")
    @Column(name = "description", length = 1000, nullable = false)
    private String description;


    @CreationTimestamp // INSERT 시 자동 생성 (현재 시간)
    @Column(name = "created_at", updatable = false) // 생성일시 (수정 불가)
    private LocalDateTime createdAt;

    @UpdateTimestamp // UPDATE 시 자동 갱신
    @Column(name = "updated_at") // 마지막 수정일시
    private LocalDateTime updatedAt;

    // (편의 메서드) 가격 변경
// - 0원 이상만 허용
    public void changePrice(Integer price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException("판매가는 0원 이상이어야 합니다.");
        }
        this.price = price;
    }

    // (편의 메서드) 할인율 변경
// - 0% 이상 99% 이하만 허용
    public void changeDiscountRate(Integer rate) {
        if (rate == null || rate < 0 || rate > 99) {
            throw new IllegalArgumentException("할인율은 0% 이상 99% 이하여야 합니다.");
        }
        this.discountRate = rate;
    }

}
