package org.example.be17pickcook.domain.refrigerator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Check;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 냉장고 아이템 엔티티: 무결성 제약 + 입력값 검증 적용 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "refrigerator_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_refrigerator_items_refrigerator_ingredient",
                        columnNames = {"refrigerator_id", "ingredient_name"}
                )
        },
        indexes = {
                @Index(name = "idx_refrigerator_items_refrigerator", columnList = "refrigerator_id")
        }
)
/** 유통기한은 null이거나(today 포함) 미래여야 한다는 DB 체크(지원 DB에 한해 적용) */
@Check(constraints = "expiration_date IS NULL OR expiration_date >= CURRENT_DATE")
public class RefrigeratorItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refrigerator_item_id")
    private Long id;

    /** 참조 무결성: 반드시 냉장고에 속해야 함 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "refrigerator_id", nullable = false)
    @NotNull(message = "냉장고 정보는 필수입니다.")
    private Refrigerator refrigerator;

    /** 재료명: 공백 금지, 최대 255자, 한글/영문/숫자/공백/하이픈만 허용 */
    @NotBlank(message = "재료명은 필수입니다.")
    @Size(max = 255, message = "재료명은 255자 이하여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s\\-]{1,255}$", message = "재료명은 한글/영문/숫자/공백/하이픈만 허용됩니다.")
    @Column(name = "ingredient_name", length = 255, nullable = false)
    private String ingredientName;

    /** 수량: 공백 금지, 최대 50자, (선택) 형식: 숫자(+소수) + 단위 */
    @NotBlank(message = "수량은 필수입니다.")
    @Size(max = 50, message = "수량은 50자 이하여야 합니다.")
    @Pattern(
            regexp = "^[0-9]+(\\.[0-9]+)?\\s?(개|봉|박스|팩|g|kg|ml|l|L)$",
            message = "수량 형식 예: 1개, 2봉, 0.5kg, 300g, 1L"
    )
    @Column(length = 50, nullable = false)
    private String quantity;

    /** 유통기한: null 허용(미관리 시) 또는 오늘/미래 */
    @FutureOrPresent(message = "유통기한은 오늘 또는 미래여야 합니다.")
    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    /** 생성/수정 시각: 자동 관리 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* ===== 도메인 편의 메서드(비즈니스 규칙 재확인) ===== */

    /** 수량 변경: 형식/길이 재검증 */
    public void changeQuantity(String quantity) {
        if (quantity == null || quantity.isBlank()) {
            throw new IllegalArgumentException("수량은 필수입니다.");
        }
        if (quantity.length() > 50) {
            throw new IllegalArgumentException("수량은 50자 이하여야 합니다.");
        }
        if (!quantity.matches("^[0-9]+(\\.[0-9]+)?\\s?(개|봉|박스|팩|g|kg|ml|l|L)$")) {
            throw new IllegalArgumentException("수량 형식 예: 1개, 2봉, 0.5kg, 300g, 1L");
        }
        this.quantity = quantity;
    }

    /** 유통기한 변경: null 허용 또는 오늘/미래 */
    public void changeExpirationDate(LocalDate expirationDate) {
        if (expirationDate != null && expirationDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("유통기한은 오늘 또는 미래여야 합니다.");
        }
        this.expirationDate = expirationDate;
    }

    /** 재료명 변경: 기본 규칙 재검증 */
    public void changeIngredientName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("재료명은 필수입니다.");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("재료명은 255자 이하여야 합니다.");
        }
        if (!name.matches("^[가-힣a-zA-Z0-9\\s\\-]{1,255}$")) {
            throw new IllegalArgumentException("재료명은 한글/영문/숫자/공백/하이픈만 허용됩니다.");
        }
        this.ingredientName = name;
    }
}
