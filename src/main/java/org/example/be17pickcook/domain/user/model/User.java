package org.example.be17pickcook.domain.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;
    private String email;
    private String password;
    private String nickname;
    private String profileImage;
    @Builder.Default
    private String role = "USER";
    @Builder.Default
    private Boolean enabled = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<EmailVerify> emailVerifyList;

    public void userVerify() {
        this.enabled = true;
    }
}
