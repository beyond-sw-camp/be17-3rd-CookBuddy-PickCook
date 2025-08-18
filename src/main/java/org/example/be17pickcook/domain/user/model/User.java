package org.example.be17pickcook.domain.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.be17pickcook.domain.likes.model.Likes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;
    private String email;
    private String password;
    private String nickname;
    private String name;         // 추가
    private String phone;
    private String profileImage;

    private String zipCode;      // 우편번호
    private String address;      // 기본 주소
    private String detailAddress; // 상세 주소

    @Builder.Default
    private String role = "USER";
    @Builder.Default
    private Boolean enabled = false;

    @Builder.Default
    private Boolean deleted = false;
    private LocalDateTime deletedAt; // 탈퇴 일시

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<EmailVerify> emailVerifyList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();

    public void userVerify() {
        this.enabled = true;
    }

    // 🔧 추가: 비밀번호 재설정과의 연관관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordReset> passwordResetList = new ArrayList<>();

    // 🔧 추가: 비밀번호 업데이트 메서드
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 🔧 추가: 소프트 삭제 메서드
    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.enabled = false; // 계정 비활성화
    }

    // 🔧 추가: 복구 메서드 (필요시)
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.enabled = false;
    }
}
