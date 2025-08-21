package org.example.be17pickcook.domain.user.repository;

import org.example.be17pickcook.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    // 🔧 추가: 탈퇴하지 않은 사용자만 조회
    @Query("SELECT u FROM User u WHERE u.email = :email AND (u.deleted IS NULL OR u.deleted = false)")
    Optional<User> findByEmailAndNotDeleted(@Param("email") String email);

    // 🔧 추가: 탈퇴하지 않은 닉네임 조회 (닉네임 중복 체크용)
    @Query("SELECT u FROM User u WHERE u.nickname = :nickname AND (u.deleted IS NULL OR u.deleted = false)")
    Optional<User> findByNicknameAndNotDeleted(@Param("nickname") String nickname);

    @Query("SELECT u FROM User u WHERE u.name = :name AND u.phone = :phone AND (u.deleted IS NULL OR u.deleted = false)")
    Optional<User> findByNameAndPhoneAndNotDeleted(@Param("name") String name, @Param("phone") String phone);

    // 🔧 추가: ID로 탈퇴하지 않은 사용자 조회
    @Query("SELECT u FROM User u WHERE u.idx = :userId AND (u.deleted IS NULL OR u.deleted = false)")
    Optional<User> findByIdAndNotDeleted(@Param("userId") Integer userId);

    // 🔧 추가: ID로 탈퇴하지 않은 사용자 존재 확인
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.idx = :userId AND (u.deleted IS NULL OR u.deleted = false)")
    boolean existsByIdAndNotDeleted(@Param("userId") Integer userId);
}
