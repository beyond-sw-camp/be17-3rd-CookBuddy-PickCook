package org.example.be17pickcook.domain.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class EmailVerify {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;
    private String uuid;

    @Builder.Default
    private LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

    // 비즈니스 로직: 만료 여부 체크
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    @ManyToOne
    @JoinColumn(name = "user_idx")
    private User user;
}
