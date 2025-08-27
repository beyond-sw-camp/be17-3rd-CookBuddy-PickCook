package org.example.be17pickcook.domain.refrigerator.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.be17pickcook.common.BaseEntity;
import org.example.be17pickcook.domain.common.model.Category;
import org.example.be17pickcook.domain.user.model.User;

import java.time.LocalDate;

/**
 * 냉장고 아이템 삭제 로그 엔티티
 * - 사용자 소비 패턴 분석용 데이터 수집
 * - 추천 시스템 기반 데이터로 활용
 */
@Entity
@Table(name = "refrigerator_item_delete_logs",
        indexes = {
                @Index(name = "idx_user_deleted_at", columnList = "user_id, deleted_at"),
                @Index(name = "idx_category_deleted_at", columnList = "category_id, deleted_at"),
                @Index(name = "idx_ingredient_name", columnList = "ingredient_name")
        })
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RefrigeratorItemDeleteLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 원본 냉장고 아이템 ID (참조용) */
    @Column(nullable = false)
    private Long originalItemId;

    /** 삭제한 사용자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /** 삭제된 재료명 */
    @Column(nullable = false, length = 255)
    private String ingredientName;

    /** 카테고리 정보 (삭제 시점 기준) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    /** 보관 위치 */
    @Column(nullable = false, length = 20)
    private String location;

    /** 수량 */
    @Column(nullable = false)
    private String quantity;

    /** 유통기한 */
    private LocalDate expirationDate;

    /** 등록일 (원본 아이템 기준) */
    @Column(nullable = false)
    private LocalDate originalCreatedDate;

    /** 삭제 시점 */
    @Column(nullable = false)
    private LocalDate deletedDate;

    /** 삭제 사유 (선택사항) */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DeleteReason deleteReason;

    /** 보관 기간 (일수) */
    @Column(nullable = false)
    private Integer storageDays;

    /** 유통기한 상태 (삭제 시점 기준) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefrigeratorItemDto.ExpirationStatus expirationStatusAtDeletion;

    /**
     * 삭제 사유 열거형
     */
    public enum DeleteReason {
        CONSUMED("소비완료"),           // 다 먹음
        EXPIRED("유통기한만료"),        // 버림
        SPOILED("상함"),              // 상해서 버림
        CLEANUP("정리"),              // 냉장고 정리
        MISTAKE("실수삭제"),           // 잘못 삭제
        OTHER("기타");               // 기타

        private final String description;
        DeleteReason(String description) {
            this.description = description;
        }
        public String getDescription() { return description; }
    }

    /**
     * 정적 팩토리 메서드: RefrigeratorItem으로부터 로그 생성
     */
    public static RefrigeratorItemDeleteLog from(RefrigeratorItem item, DeleteReason reason) {
        LocalDate now = LocalDate.now();
        LocalDate createdDate = item.getCreatedAt().toLocalDate();

        return RefrigeratorItemDeleteLog.builder()
                .originalItemId(item.getId())
                .user(item.getUser())
                .ingredientName(item.getIngredientName())
                .category(item.getCategory())
                .location(item.getLocation())
                .quantity(item.getQuantity())
                .expirationDate(item.getExpirationDate())
                .originalCreatedDate(createdDate)
                .deletedDate(now)
                .deleteReason(reason)
                .storageDays((int) java.time.temporal.ChronoUnit.DAYS.between(createdDate, now))
                .expirationStatusAtDeletion(calculateExpirationStatus(item.getExpirationDate()))
                .build();
    }

    /**
     * 삭제 시점의 유통기한 상태 계산
     */
    private static RefrigeratorItemDto.ExpirationStatus calculateExpirationStatus(LocalDate expirationDate) {
        if (expirationDate == null) return RefrigeratorItemDto.ExpirationStatus.FRESH;

        LocalDate today = LocalDate.now();
        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, expirationDate);

        if (daysUntil < 0) return RefrigeratorItemDto.ExpirationStatus.EXPIRED;
        if (daysUntil <= 1) return RefrigeratorItemDto.ExpirationStatus.URGENT;
        if (daysUntil <= 3) return RefrigeratorItemDto.ExpirationStatus.EXPIRING_SOON;
        return RefrigeratorItemDto.ExpirationStatus.FRESH;
    }
}