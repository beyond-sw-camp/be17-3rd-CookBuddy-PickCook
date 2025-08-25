package org.example.be17pickcook.domain.likes.repository;

import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.likes.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdxAndTargetTypeAndTargetId(Integer userIdx, LikeTargetType targetType, Long targetId);
    Integer countByTargetTypeAndTargetId(LikeTargetType targetType, Long targetId);
    void deleteByUserIdxAndTargetTypeAndTargetId(Integer userIdx, LikeTargetType targetType, Long targetId);
    boolean existsByUserIdxAndTargetTypeAndTargetId(Integer userIdx, LikeTargetType targetType, Long targetId);

    // 좋아요 개수 조회
    @Query("SELECT l.targetId, COUNT(l) FROM Like l " +
            "WHERE l.targetType = :targetType AND l.targetId IN :recipeIds " +
            "GROUP BY l.targetId")
    List<Object[]> countLikesByRecipeIds(@Param("targetType") LikeTargetType targetType,
                                         @Param("recipeIds") List<Long> recipeIds);

    // 사용자별 좋아요 여부
    @Query("SELECT l.targetId FROM Like l " +
            "WHERE l.targetType = :targetType AND l.user.idx = :userId AND l.targetId IN :recipeIds")
    List<Long> findLikedRecipeIdsByUser(@Param("targetType") LikeTargetType targetType,
                                           @Param("userId") Integer userId,
                                           @Param("recipeIds") List<Long> recipeIds);

}
