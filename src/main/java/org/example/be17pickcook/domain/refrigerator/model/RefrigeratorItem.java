package org.example.be17pickcook.domain.refrigerator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.be17pickcook.domain.common.model.Category;
import org.example.be17pickcook.domain.user.model.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Check;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 냉장고 아이템 엔티티 (최종 버전)
 * - 사용자별 식재료 관리
 * - 카테고리 연관관계 (FK)
 * - 소프트 삭제 지원
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "refrigerator_items",
        indexes = {
                @Index(name = "idx_refrigerator_items_user_active", columnList = "user_id, is_deleted"),
                @Index(name = "idx_refrigerator_items_category", columnList = "category_id"),
                @Index(name = "idx_refrigerator_items_location", columnList = "location"),
                @Index(name = "idx_refrigerator_items_expiration", columnList = "expiration_date"),
                @Index(name = "idx_refrigerator_items_deleted_at", columnList = "deleted_at")
        }
)
/** 유통기한은 null이거나(today 포함) 미래여야 한다는 DB 체크 */
//@Check(constraints = "expiration_date IS NULL OR expiration_date >= CURRENT_DATE")
public class RefrigeratorItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refrigerator_id")
    private Long id;

    // =================================================================
    // 연관관계 필드
    // =================================================================

    /** 소유자: 반드시 사용자에 속해야 함 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "사용자 정보는 필수입니다.")
    private User user;

    /** 카테고리: Category 테이블과 연관관계 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "카테고리는 필수입니다.")
    private Category category;

    // =================================================================
    // 기본 정보 필드
    // =================================================================

    /** 재료명: 공백 금지, 최대 255자 */
    @NotBlank(message = "재료명은 필수입니다.")
    @Size(max = 255, message = "재료명은 255자 이하여야 합니다.")
    @Column(name = "ingredient_name", length = 255, nullable = false)
    private String ingredientName;

    /** 재고위치: 실외저장소/냉장실/냉동실 */
    @NotBlank(message = "재고위치는 필수입니다.")
    @Pattern(regexp = "^(실외저장소|냉장실|냉동실)$", message = "재고위치는 실외저장소, 냉장실, 냉동실 중 하나여야 합니다.")
    @Column(name = "location", length = 20, nullable = false)
    private String location;

    /** 수량: 정수형 */
    @NotBlank(message = "수량은 필수입니다.")
    @Size(max = 20, message = "수량은 20자 이하여야 합니다.")
    @Column(name = "quantity", nullable = false)
    private String quantity;

    /** 유통기한: null 허용(미관리 시) 또는 오늘/미래 */
    @FutureOrPresent(message = "유통기한은 오늘 또는 미래여야 합니다.")
    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    // =================================================================
    // 시간 정보 필드
    // =================================================================

    /** 생성/수정 시각: 자동 관리 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =================================================================
    // 소프트 삭제 필드
    // =================================================================

    /** 삭제 여부 */
    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /** 삭제 시점 */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // =================================================================
    // 도메인 편의 메서드(비즈니스 규칙)
    // =================================================================

    /** 재료명 변경 */
    public void changeIngredientName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("재료명은 필수입니다.");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("재료명은 255자 이하여야 합니다.");
        }
        this.ingredientName = name;
    }

    /** 수량 변경 */
    public void changeQuantity(String quantity) {
        if (quantity == null || quantity.isBlank()) {
            throw new IllegalArgumentException("수량은 필수입니다.");
        }
        if (quantity.length() > 20) {
            throw new IllegalArgumentException("수량은 20자 이하여야 합니다.");
        }
        this.quantity = quantity;
    }

    /** 유통기한 변경 */
    public void changeExpirationDate(LocalDate expirationDate) {
        if (expirationDate != null && expirationDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("유통기한은 오늘 또는 미래여야 합니다.");
        }
        this.expirationDate = expirationDate;
    }

    /** 카테고리 변경 */
    public void changeCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }
        this.category = category;
    }

    /** 재고위치 변경 */
    public void changeLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("재고위치는 필수입니다.");
        }
        if (!location.matches("^(실외저장소|냉장실|냉동실)$")) {
            throw new IllegalArgumentException("재고위치는 실외저장소, 냉장실, 냉동실 중 하나여야 합니다.");
        }
        this.location = location;
    }

    /** 소프트 삭제 처리 */
    public void markAsDeleted() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /** 소프트 삭제 복원 (실행 취소) */
    public void restoreFromDeleted() {
        this.isDeleted = false;
        this.deletedAt = null;
    }

    // =================================================================
    // 비즈니스 조회 메서드
    // =================================================================

    /** 유통기한 임박 여부 확인 (3일 이내) */
    public boolean isExpirationSoon() {
        if (expirationDate == null) {
            return false;
        }
        return expirationDate.isBefore(LocalDate.now().plusDays(4));
    }

    /** 유통기한 만료 여부 확인 */
    public boolean isExpired() {
        if (expirationDate == null) {
            return false;
        }
        return expirationDate.isBefore(LocalDate.now());
    }

    /** 유통기한까지 남은 일수 계산 */
    public Integer getDaysUntilExpiration() {
        if (expirationDate == null) {
            return null;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
    }
}