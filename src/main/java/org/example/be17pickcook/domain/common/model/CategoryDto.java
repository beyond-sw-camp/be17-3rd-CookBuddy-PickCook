package org.example.be17pickcook.domain.common.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리 DTO 모음
 * - Response: 조회 시 사용
 * - Request: 추가 시 사용 (관리자용)
 * - Summary: 통계 정보 포함
 */
public class CategoryDto {

    // =================================================================
    // 기본 조회 응답 DTO
    // =================================================================

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        /** 카테고리 ID */
        private Long id;

        /** 카테고리명 */
        private String name;
    }

    // =================================================================
    // 통계 정보 포함 응답 DTO (사용자별 카테고리 사용 현황)
    // =================================================================

    @Getter
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {

        /** 카테고리 ID */
        private Long id;

        /** 카테고리명 */
        private String name;

        /** 해당 카테고리의 냉장고 아이템 개수 */
        private Integer itemCount;

        /** 유통기한 임박 아이템 개수 (3일 이내) */
        private Integer expiringItemCount;
    }

    // =================================================================
    // 추가 요청 DTO (관리자용 - 향후 확장)
    // =================================================================

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        /** 카테고리명: 필수, 공백 금지, 중복 불가 */
        @NotBlank(message = "카테고리명은 필수입니다.")
        @Size(max = 50, message = "카테고리명은 50자 이하여야 합니다.")
        private String name;
    }

    // =================================================================
    // 수정 요청 DTO (관리자용 - 향후 확장)
    // =================================================================

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {

        /** 카테고리명: 선택사항 */
        @Size(max = 50, message = "카테고리명은 50자 이하여야 합니다.")
        private String name;
    }
}