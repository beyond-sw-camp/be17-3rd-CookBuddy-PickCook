package org.example.be17pickcook.domain.user.model;

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

public class UserDto {

    // 🔐 로그인 DTO
    @Getter
    public static class Login {
        private String email;
        private String password;
    }

    // 👤 회원가입 DTO
    @Getter
    public static class Register {
        private String email;
        private String nickname;
        private String password;
        private String name;
        private String phone;
        private String zipCode;      // 우편번호
        private String address;      // 기본 주소
        private String detailAddress; // 상세 주소
    }

    // 🔧 추가: 회원탈퇴 요청 DTO
    @Getter
    public static class WithdrawRequest {
        private String password;        // 본인 확인용 비밀번호
        private String reason;          // 탈퇴 사유 (선택)
        private Boolean confirmWithdraw; // 탈퇴 확인 체크박스
    }

    // 🔧 추가: 회원탈퇴 응답 DTO
    @Getter
    @Builder
    public static class WithdrawResponse {
        private String message;
        private LocalDateTime withdrawnAt;
        private String email; // 마스킹된 이메일
    }

    // 📝 프로필 수정 DTO
    @Getter
    public static class UpdateProfile {
        private String nickname;
        private String name;
        private String phone;
        private String profileImage;
        private String zipCode;      // 우편번호
        private String address;      // 기본 주소
        private String detailAddress; // 상세 주소
    }

    // 📤 응답 DTO
    @Getter
    @Builder
    public static class Response {
        private Integer idx;
        private String email;
        private String nickname;
        private String name;
        private String phone;
        private String profileImage;
        private String zipCode;      // 우편번호
        private String address;      // 기본 주소
        private String detailAddress; // 상세 주소
    }

    // 🔍 아이디 찾기 요청 DTO
    @Getter
    public static class FindEmailRequest {
        private String name;
        private String phone;
    }

    // 📧 아이디 찾기 응답 DTO
    @Getter
    @Builder
    public static class FindEmailResponse {
        private String maskedEmail;  // 마스킹된 이메일 (화면 표시용)
        private String email;        // 실제 이메일 (힌트용)
    }

    // 🔑 비밀번호 재설정 요청 DTO
    @Getter
    public static class PasswordResetRequest {
        private String email;
    }

    // 🔒 비밀번호 재설정 DTO
    @Getter
    public static class ResetPassword {
        private String token;
        private String newPassword;
        private String confirmPassword;
    }

    // 🔐 인증 사용자 DTO (Security + OAuth2)
    @Getter
    @Builder
    public static class AuthUser implements UserDetails, OAuth2User {
        private Integer idx;
        private String email;
        private String password;
        private String nickname;
        private String name;
        private String phone;
        private String profileImage;
        private String zipCode;      // 우편번호
        private String address;      // 기본 주소
        private String detailAddress; // 상세 주소
        private Boolean enabled;
        private Map<String, Object> attributes;

        // OAuth2User 구현
        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public String getName() {
            return nickname; // OAuth2에서 사용하는 name은 nickname
        }

        // UserDetails 구현
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        @Override
        public String getUsername() {
            return email; // UserDetails에서 사용하는 username은 email
        }

        @Override
        public String getPassword() {
            return password; // 🔧 수정: {noop} 제거 (이미 암호화된 상태)
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return enabled != null ? enabled : false;
        }

        // 🔧 삭제: toEntity() 메서드 제거 - MapStruct 사용
    }
}