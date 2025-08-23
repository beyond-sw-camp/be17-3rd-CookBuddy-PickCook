package org.example.be17pickcook.domain.refrigerator.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.be17pickcook.domain.common.model.CategoryDto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 냉장고 아이템 DTO 모음
 * - Request: 추가 시 사용
 * - Response: 조회 시 사용
 * - Update: 수정 시 사용
 * - Filter: 검색/필터링 시 사용
 */
public class RefrigeratorItemDto {

    // =================================================================
    // 추가 요청 DTO
    // =================================================================

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        /** 재료명: 필수, 공백 금지 */
        @NotBlank(message = "재료명은 필수입니다.")
        @Size(max = 255, message = "재료명은 255자 이하여야 합니다.")
        private String ingredientName;

        /** 카테고리 ID: 필수 */
        @NotNull(message = "카테고리는 필수입니다.")
        @Positive(message = "올바른 카테고리를 선택해주세요.")
        private Long categoryId;

        /** 재고위치: 필수 */
        @NotBlank(message = "재고위치는 필수입니다.")
        @Pattern(regexp = "^(실외저장소|냉장실|냉동실)$",
                message = "재고위치는 실외저장소, 냉장실, 냉동실 중 하나여야 합니다.")
        private String location;

        /** 수량: 필수, 양수 */
        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 1 이상이어야 합니다.")
        private Integer quantity;

        /** 유통기한: 선택사항, 미래 날짜만 허용 */
        @Future(message = "유통기한은 오늘 이후의 날짜여야 합니다.")
        private LocalDate expirationDate;
    }

    // =================================================================
    // 조회 응답 DTO
    // =================================================================

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        /** 아이템 ID */
        private Long id;

        /** 재료명 */
        private String ingredientName;

        /** 카테고리 정보 */
        private CategoryDto.Response category;

        /** 재고위치 */
        private String location;

        /** 수량 */
        private Integer quantity;

        /** 유통기한 */
        private LocalDate expirationDate;

        /** 등록일시 */
        private LocalDateTime createdAt;

        /** 수정일시 */
        private LocalDateTime updatedAt;

        /** 유통기한 상태 (계산된 값) */
        private ExpirationStatus expirationStatus;

        /** 유통기한까지 남은 일수 (계산된 값) */
        private Integer daysUntilExpiration;
    }

    // =================================================================
    // 수정 요청 DTO
    // =================================================================

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {

        /** 재료명: 선택사항 */
        @Size(max = 255, message = "재료명은 255자 이하여야 합니다.")
        private String ingredientName;

        /** 카테고리 ID: 선택사항 */
        @Positive(message = "올바른 카테고리를 선택해주세요.")
        private Long categoryId;

        /** 재고위치: 선택사항 */
        @Pattern(regexp = "^(실외저장소|냉장실|냉동실)$",
                message = "재고위치는 실외저장소, 냉장실, 냉동실 중 하나여야 합니다.")
        private String location;

        /** 수량: 선택사항, 양수만 허용 */
        @Positive(message = "수량은 1 이상이어야 합니다.")
        private Integer quantity;

        /** 유통기한: 선택사항, 미래 날짜만 허용 */
        @Future(message = "유통기한은 오늘 이후의 날짜여야 합니다.")
        private LocalDate expirationDate;
    }

    // =================================================================
    // 검색/필터링 요청 DTO
    // =================================================================

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filter {

        private String keyword;
        private Long categoryId;
        // location 필드 삭제됨
        private ExpirationStatus expirationStatus;

        @Builder.Default
        private SortType sortType = SortType.EXPIRATION_DATE; // 기본값 변경

        @Builder.Default
        private SortDirection sortDirection = SortDirection.ASC;
    }

    // =================================================================
    // 일괄 처리 DTO (구매 → 냉장고 등록)
    // =================================================================

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkRequest {

        /** 여러 아이템 정보 */
        @NotNull(message = "등록할 아이템 목록은 필수입니다.")
        @Size(min = 1, message = "최소 1개 이상의 아이템을 등록해야 합니다.")
        private java.util.List<@Valid Request> items;
    }

    // =================================================================
    // 열거형 정의
    // =================================================================

    /** 유통기한 상태 */
    public enum ExpirationStatus {
        FRESH("신선"),           // 7일 이상 남음
        EXPIRING_SOON("임박"),   // 3-7일 남음
        URGENT("긴급"),         // 1-2일 남음
        EXPIRED("만료");        // 오늘 이후 또는 지남

        private final String description;

        ExpirationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /** 정렬 기준 */
    public enum SortType {
        EXPIRATION_DATE("유통기한순"),
        CREATED_DATE("등록일순");

        private final String description;

        SortType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /** 정렬 방향 */
    public enum SortDirection {
        ASC("오름차순"),
        DESC("내림차순");

        private final String description;

        SortDirection(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // =================================================================
    // 통계 관련 DTO
    // =================================================================

    /**
     * 카테고리별 통계 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStat {

        /** 카테고리 ID */
        private Long categoryId;

        /** 카테고리명 */
        private String categoryName;

        /** 해당 카테고리의 아이템 개수 */
        private Integer itemCount;
    }

    /**
     * 위치별 통계 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationStat {

        /** 저장 위치 */
        private String location;

        /** 해당 위치의 아이템 개수 */
        private Integer itemCount;
    }

    /**
     * 유통기한 임박 통계 DTO (카테고리별)
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpiringCategoryStat {

        /** 카테고리 ID */
        private Long categoryId;

        /** 임박한 아이템 개수 */
        private Integer expiringCount;

        /** 기준 일수 (예: 3일) */
        private Integer targetDays;
    }
}