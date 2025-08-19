package org.example.be17pickcook.domain.scrap.repository;

import org.example.be17pickcook.domain.scrap.model.ScrapTargetType;
import org.example.be17pickcook.domain.scrap.model.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    Optional<Scrap> findByUserIdxAndTargetTypeAndTargetId(Integer userIdx, ScrapTargetType targetType, Long targetId);
    Integer countByTargetTypeAndTargetId(ScrapTargetType targetType, Long targetId);
    void deleteByUserIdxAndTargetTypeAndTargetId(Integer userIdx, ScrapTargetType targetType, Long targetId);
    boolean existsByUserIdxAndTargetTypeAndTargetId(Integer userIdx, ScrapTargetType targetType, Long targetId);
}
