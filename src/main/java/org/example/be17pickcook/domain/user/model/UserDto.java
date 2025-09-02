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
    @Builder(toBuilder = true)
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
            return name;
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
    public static class ResetPasswordRequest {
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

    @Schema(description = "마이페이지 비밀번호 변경용 현재 비밀번호 확인 요청")
    @Getter
    public static class CurrentPasswordRequest {
        @Schema(description = "현재 비밀번호",
                example = "currentPassword123!")
        @NotBlank(message = "현재 비밀번호를 입력해주세요")
        private String currentPassword;

        @Schema(description = "새로운 비밀번호",
                example = "newPassword123!")
        @NotBlank(message = "새 비밀번호를 입력해주세요")
        @Pattern(message = "비밀번호는 영문 대소문자, 숫자, 특수문자(!@#$%^&*())를 조합해 8~20자로 생성해주세요.",
                regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()]).{8,20}$")
        private String newPassword;

        @Schema(description = "새 비밀번호 확인",
                example = "newPassword123!")
        @NotBlank(message = "비밀번호 확인을 입력해주세요")
        private String confirmPassword;

        // 비밀번호 일치 검증을 위한 커스텀 검증 메서드
        @Schema(hidden = true)
        @AssertTrue(message = "비밀번호가 일치하지 않습니다")
        public boolean isPasswordMatching() {
            if (newPassword == null || confirmPassword == null) {
                return true;
            }
            return newPassword.equals(confirmPassword);
        }
    }

    @Schema(description = "비밀번호 재설정 이메일 발송 요청 정보")
    @Getter
    public static class PasswordResetEmailRequest {
        @Schema(description = "비밀번호를 재설정할 이메일 (필수)",
                example = "user@example.com")
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;
    }

    // =================================================================
    // 토큰 관련 DTO
    // =================================================================

    @Schema(description = "토큰 검증 응답 정보")
    @Getter
    @Builder
    public static class TokenValidationResponse {
        @Schema(description = "토큰 유효성", example = "true")
        private Boolean valid;

        @Schema(description = "토큰 타입 (EMAIL_RESET 또는 INTERNAL_RESET)",
                example = "EMAIL_RESET")
        private String tokenType;

        @Schema(description = "토큰 만료시간까지 남은 분", example = "25")
        private Long remainingMinutes;
    }

    @Schema(description = "내부 토큰 생성 응답 정보")
    @Getter
    @Builder
    public static class InternalTokenResponse {
        @Schema(description = "생성된 토큰",
                example = "abc123-def456-ghi789")
        private String token;

        @Schema(description = "토큰 타입", example = "INTERNAL_RESET")
        private String tokenType;

        @Schema(description = "토큰 만료시간 (분)", example = "10")
        private Integer expirationMinutes;

        @Schema(description = "Vue 컴포넌트로 리다이렉트할 URL",
                example = "/reset-password?token=abc123&type=internal")
        private String redirectUrl;
    }

    // =================================================================
    // OAuth2 관련 DTO
    // =================================================================

    @Schema(description = "OAuth2 비밀번호 변경 리다이렉트 응답")
    @Getter
    @Builder
    public static class OAuth2RedirectResponse {
        @Schema(description = "안내 메시지",
                example = "소셜 로그인 사용자는 카카오 계정에서 비밀번호를 변경해주세요.")
        private String message;

        @Schema(description = "카카오 계정 관리 페이지 URL",
                example = "https://accounts.kakao.com/weblogin/account/info")
        private String redirectUrl;

        @Schema(description = "OAuth2 제공자", example = "KAKAO")
        private String provider;
    }

    // =================================================================
    // 비밀번호 재설정 응답 DTO
    // =================================================================

    @Schema(description = "비밀번호 재설정 완료 응답")
    @Getter
    @Builder
    public static class PasswordResetResponse {
        @Schema(description = "완료 메시지",
                example = "비밀번호가 성공적으로 변경되었습니다.")
        private String message;

        @Schema(description = "재설정 완료 시간")
        private LocalDateTime resetAt;

        @Schema(description = "JWT 토큰 무효화 여부", example = "true")
        private Boolean jwtInvalidated;

        @Schema(description = "로그인 페이지 리다이렉트 안내",
                example = "모든 기기에서 재로그인이 필요합니다.")
        private String redirectMessage;
    }

    // =================================================================
    // 에러 응답 관련 DTO (향후 확장용)
    // =================================================================

    @Schema(description = "비밀번호 재설정 에러 응답")
    @Getter
    @Builder
    public static class PasswordResetErrorResponse {
        @Schema(description = "에러 메시지",
                example = "토큰이 만료되었습니다.")
        private String message;

        @Schema(description = "에러 코드", example = "30302")
        private Integer errorCode;

        @Schema(description = "다시 시도 가능 여부", example = "true")
        private Boolean retryable;

        @Schema(description = "권장 액션",
                example = "비밀번호 재설정을 다시 요청해주세요.")
        private String recommendedAction;
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