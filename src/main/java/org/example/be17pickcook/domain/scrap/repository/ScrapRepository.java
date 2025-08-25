package org.example.be17pickcook.domain.scrap.repository;

import org.example.be17pickcook.domain.scrap.model.ScrapTargetType;
import org.example.be17pickcook.domain.scrap.model.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    Optional<Scrap> findByUserIdxAndTargetTypeAndTargetId(Integer userIdx, ScrapTargetType targetType, Long targetId);
    Integer countByTargetTypeAndTargetId(ScrapTargetType targetType, Long targetId);
    void deleteByUserIdxAndTargetTypeAndTargetId(Integer userIdx, ScrapTargetType targetType, Long targetId);
    boolean existsByUserIdxAndTargetTypeAndTargetId(Integer userIdx, ScrapTargetType targetType, Long targetId);

    // 사용자별 스크랩 여부
    @Query("SELECT s.targetId FROM Scrap s " +
            "WHERE s.targetType = :targetType AND s.user.idx = :userId AND s.targetId IN :recipeIds")
    List<Long> findScrappedRecipeIdsByUser(@Param("targetType") ScrapTargetType targetType,
                                              @Param("userId") Integer userId,
                                              @Param("recipeIds") List<Long> recipeIds);
}
