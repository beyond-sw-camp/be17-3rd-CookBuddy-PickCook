package org.example.be17pickcook.domain.refrigerator.model;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "냉장고 식재료 관련 DTO 클래스들")
public class RefrigeratorItemDto {

    // =================================================================
    // 추가 요청 DTO
    // =================================================================

    @Schema(description = "냉장고 식재료 등록 요청 정보")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        /** 재료명: 필수, 공백 금지 */
        @Schema(description = "식재료명 (필수)",
                example = "신선한 상추",
                maxLength = 255,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "재료명은 필수입니다.")
        @Size(max = 255, message = "재료명은 255자 이하여야 합니다.")
        private String ingredientName;

        /** 카테고리 ID: 필수 */
        @Schema(description = "카테고리 ID (필수)",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "카테고리는 필수입니다.")
        @Positive(message = "올바른 카테고리를 선택해주세요.")
        private Long categoryId;

        /** 재고위치: 필수 */
        @Schema(description = "보관 위치 (필수)",
                example = "냉장실",
                allowableValues = {"실외저장소", "냉장실", "냉동실"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "재고위치는 필수입니다.")
        @Pattern(regexp = "^(실외저장소|냉장실|냉동실)$",
                message = "재고위치는 실외저장소, 냉장실, 냉동실 중 하나여야 합니다.")
        private String location;

        /** 수량: 필수, 양수 */
        @Schema(description = "수량 (필수, 자유 형식)",
                example = "1봉지",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "수량은 필수입니다.")
        private String quantity;

        /** 유통기한: 선택사항, 미래 날짜만 허용 */
        @Schema(description = "유통기한 (선택사항)",
                example = "2025-02-15",
                format = "date")
        @FutureOrPresent(message = "유통기한은 오늘 또는 미래 날짜여야 합니다.")
        private LocalDate expirationDate;
    }

    // =================================================================
    // 조회 응답 DTO
    // =================================================================

    @Schema(description = "냉장고 식재료 조회 응답 정보")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        /** 아이템 ID */
        @Schema(description = "식재료 고유 ID", example = "1")
        private Long id;

        /** 재료명 */
        @Schema(description = "식재료명", example = "신선한 상추")
        private String ingredientName;

        /** 카테고리 정보 */
        @Schema(description = "카테고리 정보")
        private CategoryDto.Response category;

        /** 재고위치 */
        @Schema(description = "보관 위치",
                example = "냉장실",
                allowableValues = {"실외저장소", "냉장실", "냉동실"})
        private String location;

        /** 수량 */
        @Schema(description = "수량", example = "1봉지")
        private String quantity;

        /** 유통기한 */
        @Schema(description = "유통기한",
                example = "2025-02-15",
                format = "date")
        private LocalDate expirationDate;

        /** 등록일시 */
        @Schema(description = "등록일시", example = "2025-01-15T10:30:00")
        private LocalDateTime createdAt;

        /** 수정일시 */
        @Schema(description = "수정일시", example = "2025-01-15T15:45:00")
        private LocalDateTime updatedAt;

        /** 유통기한 상태 (계산된 값) */
        @Schema(description = "유통기한 상태",
                example = "FRESH",
                implementation = ExpirationStatus.class)
        private ExpirationStatus expirationStatus;

        /** 유통기한까지 남은 일수 (계산된 값) */
        @Schema(description = "유통기한까지 남은 일수", example = "7")
        private Integer daysUntilExpiration;
    }

    // =================================================================
    // 수정 요청 DTO
    // =================================================================

    @Schema(description = "냉장고 식재료 수정 요청 정보 (모든 필드 선택사항)")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {

        /** 재료명: 선택사항 */
        @Schema(description = "식재료명 (선택사항)",
                example = "프리미엄 상추",
                maxLength = 255)
        @Size(max = 255, message = "재료명은 255자 이하여야 합니다.")
        private String ingredientName;

        /** 카테고리 ID: 선택사항 */
        @Schema(description = "카테고리 ID", example = "2")
        @Positive(message = "올바른 카테고리를 선택해주세요.")
        private Long categoryId;

        /** 재고위치: 선택사항 */
        @Schema(description = "보관 위치 (선택사항)",
                example = "냉동실",
                allowableValues = {"실외저장소", "냉장실", "냉동실"})
        @Pattern(regexp = "^(실외저장소|냉장실|냉동실)$",
                message = "재고위치는 실외저장소, 냉장실, 냉동실 중 하나여야 합니다.")
        private String location;

        /** 수량: 선택사항, 양수만 허용 */
        @Schema(description = "수량 (선택사항)", example = "2봉지")
        @NotBlank(message = "수량은 필수입니다.")
        private String quantity;

        /** 유통기한: 선택사항, 미래 날짜만 허용 */
        @Schema(description = "유통기한 (선택사항)",
                example = "2025-03-15",
                format = "date")
        @FutureOrPresent(message = "유통기한은 오늘 또는 미래 날짜여야 합니다.")
        private LocalDate expirationDate;
    }

    // =================================================================
    // 검색/필터링 요청 DTO
    // =================================================================

    @Schema(description = "냉장고 식재료 검색/필터링 조건")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filter {

        @Schema(description = "검색 키워드 (식재료명)", example = "상추")
        private String keyword;

        @Schema(description = "카테고리 ID 필터", example = "1")
        private Long categoryId;

        @Schema(description = "유통기한 상태 필터",
                example = "EXPIRING_SOON",
                implementation = ExpirationStatus.class)
        private ExpirationStatus expirationStatus;

        @Schema(description = "정렬 기준",
                example = "EXPIRATION_DATE",
                implementation = SortType.class)
        @Builder.Default
        private SortType sortType = SortType.EXPIRATION_DATE; // 기본값 변경

        @Schema(description = "정렬 방향",
                example = "ASC",
                implementation = SortDirection.class)
        @Builder.Default
        private SortDirection sortDirection = SortDirection.ASC;
    }

    // =================================================================
    // 일괄 처리 DTO (구매 → 냉장고 등록)
    // =================================================================

    @Schema(description = "냉장고 식재료 일괄 등록 요청 정보")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkRequest {

        /** 여러 아이템 정보 */
        @Schema(description = "등록할 식재료 목록",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "등록할 아이템 목록은 필수입니다.")
        @Size(min = 1, message = "최소 1개 이상의 아이템을 등록해야 합니다.")
        private java.util.List<@Valid Request> items;
    }

    // =================================================================
    // 열거형 정의
    // =================================================================

    /** 유통기한 상태 */
    @Schema(description = "유통기한 상태 분류")
    public enum ExpirationStatus {
        @Schema(description = "신선 (4일 이상 남음)")
        FRESH("신선"),           // 4일 이상 남음

        @Schema(description = "임박 (3일 남음)")
        EXPIRING_SOON("임박"),   // 3일 남음

        @Schema(description = "긴급 (오늘~2일 남음)")
        URGENT("긴급"),         // 오늘~2일 남음

        @Schema(description = "만료 (지남)")
        EXPIRED("만료");        // 지남

        private final String description;

        ExpirationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /** 정렬 기준 */
    @Schema(description = "정렬 기준 옵션")
    public enum SortType {

        @Schema(description = "유통기한순 정렬")
        EXPIRATION_DATE("유통기한순"),

        @Schema(description = "등록일순 정렬")
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
    @Schema(description = "정렬 방향 옵션")
    public enum SortDirection {

        @Schema(description = "오름차순 정렬")
        ASC("오름차순"),

        @Schema(description = "내림차순 정렬")
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
    @Schema(description = "카테고리별 식재료 통계 정보")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStat {

        /** 카테고리 ID */
        @Schema(description = "카테고리 ID", example = "1")
        private Long categoryId;

        /** 카테고리명 */
        @Schema(description = "카테고리명", example = "채소")
        private String categoryName;

        /** 해당 카테고리의 아이템 개수 */
        @Schema(description = "해당 카테고리의 식재료 개수", example = "5")
        private Integer itemCount;
    }

    /**
     * 위치별 통계 DTO
     */
    @Schema(description = "보관 위치별 식재료 통계 정보")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationStat {

        /** 저장 위치 */
        @Schema(description = "보관 위치",
                example = "냉장실",
                allowableValues = {"실외저장소", "냉장실", "냉동실"})
        private String location;

        /** 해당 위치의 아이템 개수 */
        @Schema(description = "해당 위치의 식재료 개수", example = "3")
        private Integer itemCount;
    }

    /**
     * 유통기한 임박 통계 DTO (카테고리별)
     */
    @Schema(description = "카테고리별 유통기한 임박 통계 정보")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpiringCategoryStat {

        /** 카테고리 ID */
        @Schema(description = "카테고리 ID", example = "1")
        private Long categoryId;

        /** 임박한 아이템 개수 */
        @Schema(description = "유통기한 임박한 식재료 개수", example = "2")
        private Integer expiringCount;

        /** 기준 일수 (예: 3일) */
        @Schema(description = "기준 일수", example = "3")
        private Integer targetDays;
    }

    @Schema(description = "냉장고 동기화 안내 메시지 정보")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncPrompt {

        /** 기본 환영 메시지 */
        @Schema(description = "기본 환영 메시지",
                example = "냉장고에 오신 것을 환영합니다!")
        private String baseMessage;

        /** 상황별 추가 메시지 */
        @Schema(description = "상황별 추가 메시지",
                example = "새로 구매한 상품이 있어요. 냉장고에 등록해보세요!")
        private String contextMessage;

        /** 메시지 타입 (정보성/경고성/액션유도) */
        @Schema(description = "메시지 타입",
                example = "ACTION",
                implementation = PromptType.class)
        private PromptType messageType;

        /** 추천 액션 (선택사항) */
        @Schema(description = "권장 액션",
                example = "지금 업데이트")
        private String recommendedAction;

        @Schema(description = "안내 메시지 타입 분류")
        public enum PromptType {

            @Schema(description = "정보성 메시지")
            INFO("ℹ️"),

            @Schema(description = "경고성 메시지")
            WARNING("⚠️"),

            @Schema(description = "액션 유도 메시지")
            ACTION("🔥");

            private final String icon;
            PromptType(String icon) { this.icon = icon; }
            public String getIcon() { return icon; }
        }
    }
}