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

    @Column(nullable = false, unique = true, length = 100)  // 기존: nullable 설정 없음
    private String email;

    @Column(length = 255)  // OAuth2 사용자는 비밀번호가 없을 수 있음
    private String password;

    @Column(nullable = false, unique = true, length = 20)  // 🔧 추가: 필수 + 유니크
    private String nickname;

    @Column(nullable = false, length = 50)  // 필수 정보로 변경
    private String name;

    @Column(nullable = false, length = 15)  // 필수 정보로 변경
    private String phone;

    @Column(length = 500)  // 선택적 정보
    private String profileImage;

    @Column(length = 10)  // 선택적 정보
    private String zipCode;

    @Column(length = 200)  // 선택적 정보
    private String address;

    @Column(length = 200)  // 선택적 정보
    private String detailAddress;

    // 필수 필드로 변경 + 기본값 설정
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String role = "USER";

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    // 소프트 삭제를 위한 필수 필드
    @Column(nullable = false)
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

    // 추가: 비밀번호 재설정과의 연관관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordReset> passwordResetList = new ArrayList<>();

    // 추가: 비밀번호 업데이트 메서드
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 추가: 소프트 삭제 메서드
    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.enabled = false; // 계정 비활성화
    }

    // 추가: 복구 메서드 (필요시)
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.enabled = false;
    }
}
