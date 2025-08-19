package org.example.be17pickcook.domain.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.be17pickcook.common.BaseEntity;
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
public class User extends BaseEntity {  // ✅ BaseEntity 상속 추가
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(length = 500)
    private String profileImage;

    @Column(length = 10)
    private String zipCode;

    @Column(length = 200)
    private String address;

    @Column(length = 200)
    private String detailAddress;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String role = "USER";

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    private LocalDateTime deletedAt; // 탈퇴 일시

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<EmailVerify> emailVerifyList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordReset> passwordResetList = new ArrayList<>();

    // 비즈니스 로직 메서드들
    public void userVerify() {
        this.enabled = true;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.enabled = false;
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.enabled = false;
    }
}