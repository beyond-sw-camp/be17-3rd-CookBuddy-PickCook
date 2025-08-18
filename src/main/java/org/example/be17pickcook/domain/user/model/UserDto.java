package org.example.be17pickcook.domain.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Schema(description = "사용자 관련 DTO 클래스들")
public class UserDto {

    // =================================================================
    // 인증 관련 DTO
    // =================================================================

    @Schema(description = "로그인 요청 정보")
    @Getter
    public static class Login {
        @Schema(description = "사용자 이메일", example = "user@example.com")
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @Schema(description = "사용자 비밀번호", example = "password123!")
        @NotBlank(message = "비밀번호를 입력해주세요")
        private String password;
    }

    @Schema(description = "인증된 사용자 정보 (내부용)")
    @Getter
    @Builder
    public static class AuthUser implements UserDetails, OAuth2User {
        @Schema(description = "사용자 고유 ID", example = "1")
        private Integer idx;

        @Schema(description = "사용자 이메일", example = "user@example.com")
        private String email;

        @Schema(description = "암호화된 비밀번호 (내부용)")
        private String password;

        @Schema(description = "사용자 닉네임", example = "쿡마스터")
        private String nickname;

        @Schema(description = "사용자 실명", example = "홍길동")
        private String name;

        @Schema(description = "사용자 전화번호", example = "010-1234-5678")
        private String phone;

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String profileImage;

        @Schema(description = "우편번호", example = "12345")
        private String zipCode;

        @Schema(description = "기본 주소", example = "서울시 강남구 테헤란로")
        private String address;

        @Schema(description = "상세 주소", example = "123번지 456호")
        private String detailAddress;

        @Schema(description = "계정 활성화 여부", example = "true")
        private Boolean enabled;

        @Schema(description = "OAuth2 속성 정보 (내부용)")
        private Map<String, Object> attributes;

        // OAuth2User 구현
        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public String getName() {
            return nickname;
        }

        // UserDetails 구현
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        @Override
        public String getUsername() {
            return email;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean isEnabled() {
            return enabled != null ? enabled : false;
        }
    }

    // =================================================================
    // 회원가입 관련 DTO
    // =================================================================

    @Schema(description = "회원가입 요청 정보")
    @Getter
    public static class Register {
        @Schema(description = "사용자 이메일 (필수)",
                example = "newuser@example.com")
        @Pattern(message = "이메일 형식을 사용해주세요",
                regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        private String email;

        @Schema(description = "사용자 닉네임 (필수, 2-20자)",
                example = "쿡마스터")
        @NotBlank(message = "닉네임을 입력해주세요")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요")
        @Pattern(message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다",
                regexp = "^[가-힣a-zA-Z0-9]+$")
        private String nickname;

        @Schema(description = "사용자 비밀번호 (필수, 8-20자, 영문+숫자+특수문자)",
                example = "password123!")
        @Pattern(message = "비밀번호는 영문 대소문자, 숫자, 특수문자(!@#$%^&*())를 조합해 8~20자로 생성해주세요.",
                regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()]).{8,20}$")
        private String password;

        @Schema(description = "사용자 실명 (필수, 한글/영문만)",
                example = "홍길동")
        @NotBlank(message = "이름을 입력해주세요")
        @Size(min = 1, max = 50, message = "이름은 50자 이하로 입력해주세요")
        @Pattern(message = "이름은 한글, 영문만 사용 가능합니다",
                regexp = "^[가-힣a-zA-Z\\s]+$")
        private String name;

        @Schema(description = "사용자 전화번호 (필수, 010-XXXX-XXXX 형식)",
                example = "010-1234-5678")
        @NotBlank(message = "전화번호를 입력해주세요")
        @Pattern(message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)",
                regexp = "^01[0-9]-[0-9]{3,4}-[0-9]{4}$")
        private String phone;

        @Schema(description = "우편번호 (선택사항, 최대 10자)",
                example = "12345")
        @Size(max = 10, message = "우편번호는 10자 이하로 입력해주세요")
        private String zipCode;

        @Schema(description = "기본 주소 (선택사항, 최대 200자)",
                example = "서울시 강남구 테헤란로")
        @Size(max = 200, message = "주소는 200자 이하로 입력해주세요")
        private String address;

        @Schema(description = "상세 주소 (선택사항, 최대 100자)",
                example = "123번지 456호")
        @Size(max = 100, message = "상세주소는 100자 이하로 입력해주세요")
        private String detailAddress;
    }

    // =================================================================
    // 프로필 수정 관련 DTO
    // =================================================================

    @Schema(description = "프로필 수정 요청 정보")
    @Getter
    public static class UpdateProfile {
        @Schema(description = "수정할 닉네임 (선택사항, 2-20자)",
                example = "새로운닉네임")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요")
        @Pattern(message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다",
                regexp = "^[가-힣a-zA-Z0-9]+$")
        private String nickname;

        @Schema(description = "수정할 실명 (선택사항, 한글/영문만)",
                example = "김철수")
        @Size(min = 1, max = 50, message = "이름은 50자 이하로 입력해주세요")
        @Pattern(message = "이름은 한글, 영문만 사용 가능합니다",
                regexp = "^[가-힣a-zA-Z\\s]+$")
        private String name;

        @Schema(description = "수정할 전화번호 (선택사항, 010-XXXX-XXXX 형식)",
                example = "010-9876-5432")
        @Pattern(message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)",
                regexp = "^01[0-9]-[0-9]{3,4}-[0-9]{4}$")
        private String phone;

        @Schema(description = "프로필 이미지 URL (선택사항)",
                example = "https://example.com/new-profile.jpg")
        @Size(max = 500, message = "프로필 이미지 URL은 500자 이하로 입력해주세요")
        private String profileImage;

        @Schema(description = "우편번호 (선택사항)",
                example = "54321")
        @Size(max = 10, message = "우편번호는 10자 이하로 입력해주세요")
        private String zipCode;

        @Schema(description = "기본 주소 (선택사항)",
                example = "부산시 해운대구 센텀로")
        @Size(max = 200, message = "주소는 200자 이하로 입력해주세요")
        private String address;

        @Schema(description = "상세 주소 (선택사항)",
                example = "789번지 101호")
        @Size(max = 100, message = "상세주소는 100자 이하로 입력해주세요")
        private String detailAddress;
    }

    @Schema(description = "사용자 정보 응답")
    @Getter
    @Builder
    public static class Response {
        @Schema(description = "사용자 고유 ID", example = "1")
        private Integer idx;

        @Schema(description = "사용자 이메일", example = "user@example.com")
        private String email;

        @Schema(description = "사용자 닉네임", example = "쿡마스터")
        private String nickname;

        @Schema(description = "사용자 실명", example = "홍길동")
        private String name;

        @Schema(description = "사용자 전화번호", example = "010-1234-5678")
        private String phone;

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String profileImage;

        @Schema(description = "우편번호", example = "12345")
        private String zipCode;

        @Schema(description = "기본 주소", example = "서울시 강남구 테헤란로")
        private String address;

        @Schema(description = "상세 주소", example = "123번지 456호")
        private String detailAddress;
    }

    // =================================================================
    // 계정 찾기 관련 DTO
    // =================================================================

    @Schema(description = "아이디(이메일) 찾기 요청 정보")
    @Getter
    public static class FindEmailRequest {
        @Schema(description = "사용자 실명 (필수, 한글/영문만)",
                example = "홍길동")
        @NotBlank(message = "이름을 입력해주세요")
        @Size(min = 1, max = 50, message = "이름은 50자 이하로 입력해주세요")
        @Pattern(message = "이름은 한글, 영문만 사용 가능합니다",
                regexp = "^[가-힣a-zA-Z\\s]+$")
        private String name;

        @Schema(description = "사용자 전화번호 (필수, 010-XXXX-XXXX 형식)",
                example = "010-1234-5678")
        @NotBlank(message = "전화번호를 입력해주세요")
        @Pattern(message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)",
                regexp = "^01[0-9]-[0-9]{3,4}-[0-9]{4}$")
        private String phone;
    }

    @Schema(description = "아이디(이메일) 찾기 응답 정보")
    @Getter
    @Builder
    public static class FindEmailResponse {
        @Schema(description = "마스킹된 이메일 (화면 표시용)", example = "tes***@example.com")
        private String maskedEmail;

        @Schema(description = "실제 이메일 (힌트용)", example = "test@example.com")
        private String email;
    }

    // =================================================================
    // 비밀번호 재설정 관련 DTO
    // =================================================================

    @Schema(description = "비밀번호 재설정 요청 정보")
    @Getter
    public static class PasswordResetRequest {
        @Schema(description = "비밀번호를 재설정할 이메일 (필수)",
                example = "user@example.com")
        @Pattern(message = "이메일 형식을 사용해주세요",
                regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        private String email;
    }

    @Schema(description = "비밀번호 재설정 실행 정보")
    @Getter
    public static class ResetPassword {
        @Schema(description = "비밀번호 재설정 토큰 (필수)",
                example = "abc123-def456-ghi789")
        @NotBlank(message = "토큰이 필요합니다")
        private String token;

        @Schema(description = "새로운 비밀번호 (필수, 8-20자, 영문+숫자+특수문자)",
                example = "newPassword123!")
        @Pattern(message = "비밀번호는 영문 대소문자, 숫자, 특수문자(!@#$%^&*())를 조합해 8~20자로 생성해주세요.",
                regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()]).{8,20}$")
        private String newPassword;

        @Schema(description = "새로운 비밀번호 확인 (필수)",
                example = "newPassword123!")
        @NotBlank(message = "비밀번호 확인을 입력해주세요")
        private String confirmPassword;

        @Schema(hidden = true)
        @AssertTrue(message = "비밀번호가 일치하지 않습니다")
        public boolean isPasswordMatching() {
            if (newPassword == null || confirmPassword == null) {
                return true;
            }
            return newPassword.equals(confirmPassword);
        }
    }

    // =================================================================
    // 회원탈퇴 관련 DTO
    // =================================================================

    @Schema(description = "회원탈퇴 요청 정보")
    @Getter
    public static class WithdrawRequest {
        @Schema(description = "현재 비밀번호 (일반 로그인 사용자만 필요)",
                example = "currentPassword123!")
        private String password;

        @Schema(description = "탈퇴 사유 (선택사항, 최대 500자)",
                example = "더 이상 서비스를 이용하지 않음")
        @Size(max = 500, message = "탈퇴 사유는 500자 이하로 입력해주세요")
        private String reason;

        @Schema(description = "탈퇴 확인 동의 (필수)",
                example = "true")
        @NotNull(message = "탈퇴 확인을 체크해주세요")
        @AssertTrue(message = "탈퇴 확인이 필요합니다")
        private Boolean confirmWithdraw;
    }

    @Schema(description = "회원탈퇴 완료 응답 정보")
    @Getter
    @Builder
    public static class WithdrawResponse {
        @Schema(description = "탈퇴 완료 메시지", example = "회원탈퇴가 완료되었습니다.")
        private String message;

        @Schema(description = "탈퇴 처리 일시", example = "2025-01-15T10:30:00")
        private LocalDateTime withdrawnAt;

        @Schema(description = "마스킹된 이메일", example = "tes***@example.com")
        private String email;
    }
}