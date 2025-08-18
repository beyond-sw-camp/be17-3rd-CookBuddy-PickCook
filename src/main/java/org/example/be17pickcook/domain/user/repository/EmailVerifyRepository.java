package org.example.be17pickcook.domain.user.repository;

import org.example.be17pickcook.domain.user.model.EmailVerify;
import org.example.be17pickcook.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerifyRepository extends JpaRepository<EmailVerify, Integer> {
    Optional<EmailVerify> findByUuid(String uuid);

    @Modifying
    @Query("DELETE FROM EmailVerify e WHERE e.user = :user")
    void deleteByUser(@Param("user") User user);
}
