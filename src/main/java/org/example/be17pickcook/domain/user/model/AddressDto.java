// 📁 org.example.be17pickcook.domain.user.model.AddressDto.java

package org.example.be17pickcook.domain.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 배송지 관련 DTO 모음
 */
public class AddressDto {

    // =================================================================
    // 배송지 추가 요청 DTO
    // =================================================================

    @Schema(description = "배송지 추가 요청 정보")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @Schema(description = "우편번호 (필수)", example = "12345")
        @NotBlank(message = "우편번호는 필수입니다.")
        @Size(max = 10, message = "우편번호는 10자 이하로 입력해주세요.")
        private String postalCode;

        @Schema(description = "도로명주소 (필수)", example = "서울특별시 강남구 테헤란로 123")
        @NotBlank(message = "도로명주소는 필수입니다.")
        @Size(max = 200, message = "도로명주소는 200자 이하로 입력해주세요.")
        private String roadAddress;

        @Schema(description = "상세주소 (필수)", example = "456호")
        @NotBlank(message = "상세주소는 필수입니다.")
        @Size(max = 100, message = "상세주소는 100자 이하로 입력해주세요.")
        private String detailAddress;

        @Schema(description = "기본배송지 설정 여부 (선택, 기본값: false)", example = "true")
        @Builder.Default
        private Boolean isDefault = false;
    }

    // =================================================================
    // 배송지 수정 요청 DTO
    // =================================================================

    @Schema(description = "배송지 수정 요청 정보")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {

        @Schema(description = "우편번호 (선택)", example = "54321")
        @Size(max = 10, message = "우편번호는 10자 이하로 입력해주세요.")
        private String postalCode;

        @Schema(description = "도로명주소 (선택)", example = "부산광역시 해운대구 센텀로 45")
        @Size(max = 200, message = "도로명주소는 200자 이하로 입력해주세요.")
        private String roadAddress;

        @Schema(description = "상세주소 (선택)", example = "789호")
        @Size(max = 100, message = "상세주소는 100자 이하로 입력해주세요.")
        private String detailAddress;

        @Schema(description = "기본배송지 설정 여부 (선택)", example = "false")
        private Boolean isDefault;
    }

    // =================================================================
    // 배송지 응답 DTO
    // =================================================================

    @Schema(description = "배송지 정보 응답")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        @Schema(description = "배송지 ID", example = "1")
        private Long addressId;

        @Schema(description = "우편번호", example = "12345")
        private String postalCode;

        @Schema(description = "도로명주소", example = "서울특별시 강남구 테헤란로 123")
        private String roadAddress;

        @Schema(description = "상세주소", example = "456호")
        private String detailAddress;

        @Schema(description = "기본배송지 여부", example = "true")
        private Boolean isDefault;

        @Schema(description = "생성일시", example = "2025-01-15T10:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "전체 주소 (읽기 전용)", example = "(12345) 서울특별시 강남구 테헤란로 123 456호")
        private String fullAddress;
    }
}