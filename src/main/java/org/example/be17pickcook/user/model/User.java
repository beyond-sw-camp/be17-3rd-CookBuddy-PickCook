package org.example.be17pickcook.user.model;

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
    private String ProfileImage;
    private String role;
    private Boolean enabled;

    @OneToMany(mappedBy = "user")
    List<EmailVerify> emailVerifyList;

    public void userVerify() {
        this.enabled = true;
    }

}
