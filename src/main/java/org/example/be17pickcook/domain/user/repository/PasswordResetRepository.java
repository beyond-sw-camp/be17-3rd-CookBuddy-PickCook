package org.example.be17pickcook.domain.user.repository;

import org.example.be17pickcook.domain.user.model.PasswordReset;
import org.example.be17pickcook.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Integer> {

    Optional<PasswordReset> findByTokenAndUsedFalse(String token);

    // 특정 사용자의 모든 미사용 토큰 무효화
    @Modifying
    @Query("UPDATE PasswordReset p SET p.used = true WHERE p.user = :user AND p.used = false")
    void markAllAsUsedByUser(@Param("user") User user);
}
