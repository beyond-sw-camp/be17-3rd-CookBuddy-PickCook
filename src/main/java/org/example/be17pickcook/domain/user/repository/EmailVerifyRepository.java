package org.example.be17pickcook.domain.user.repository;

import org.example.be17pickcook.domain.user.model.EmailVerify;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerifyRepository extends JpaRepository<EmailVerify, Integer> {
    Optional<EmailVerify> findByUuid(String uuid);
}
