package org.example.be17pickcook.domain.likes.repository;

import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.likes.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdxAndTargetTypeAndTargetId(Integer userIdx, LikeTargetType targetType, Long targetId);
    Integer countByTargetTypeAndTargetId(LikeTargetType targetType, Long targetId);
    void deleteByUserIdxAndTargetTypeAndTargetId(Integer userIdx, LikeTargetType targetType, Long targetId);
    boolean existsByUserIdxAndTargetTypeAndTargetId(Integer userIdx, LikeTargetType targetType, Long targetId);
}
