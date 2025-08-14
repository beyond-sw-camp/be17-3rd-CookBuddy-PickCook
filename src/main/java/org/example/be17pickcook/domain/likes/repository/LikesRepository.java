package org.example.be17pickcook.domain.likes.repository;

import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.likes.model.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    Optional<Likes> findByUserIdxAndTargetTypeAndTargetId(Integer userIdx, LikeTargetType targetType, Long targetId);
    Integer countByTargetTypeAndTargetId(LikeTargetType targetType, Long targetId);
    void deleteByUserIdxAndTargetTypeAndTargetId(Integer userIdx, LikeTargetType targetType, Long targetId);
}
