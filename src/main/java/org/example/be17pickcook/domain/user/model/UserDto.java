package org.example.be17pickcook.domain.user.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class UserDto {
    @Getter
    public static class Login {
        private String email;
        private String password;
    }

    @Getter
    public static class Register {
        private String email;
        private String nickname;
        private String password;
    }

    @Getter
    public static class UpdateProfile {
        private String nickname;
        private String name;
        private String phone;
        private String profileImage;
    }

    @Getter
    @Builder
    public static class Response {
        private Integer idx;
        private String email;
        private String nickname;
        private String name;        // 추가
        private String phone;       // 추가
        private String profileImage; // 추가
    }

    @Getter
    @Builder
    public static class AuthUser implements UserDetails, OAuth2User {
        private Integer idx;
        private String email;
        private String password;
        private String nickname;
        private String name;           // 추가: 이름 필드
        private String phone;          // 추가: 연락처 필드
        private String profileImage;   // 추가: 프로필 이미지 필드
        private Boolean enabled;
        private Map<String, Object> attributes;

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public boolean isEnabled() {
            return enabled != null ? enabled : false;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        @Override
        public String getUsername() {
            return email;
        }

        public String getPassword() {
            return "{noop}"+password;
        }

        @Override
        public String getName() {
            return nickname;
        }

        public User toEntity() {
            return User.builder()
                    .idx(idx)
                    .build();
        }
    }
}