package org.example.be17pickcook.domain.refrigerator.repository;

import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 냉장고 아이템 리포지토리
 * - 소프트 삭제 지원
 * - 검색, 필터링, 정렬 기능 (QueryDSL 확장)
 */
public interface RefrigeratorItemRepository extends JpaRepository<RefrigeratorItem, Long>, RefrigeratorItemRepositoryCustom {

    // =================================================================
    // 기본 CRUD (소프트 삭제 고려)
    // =================================================================

    /** 사용자별 활성(삭제되지 않은) 냉장고 아이템 전체 조회 */
    List<RefrigeratorItem> findByUserIdxAndIsDeletedFalseOrderByLocationAscExpirationDateAsc(Integer userId);

    /** 사용자별 특정 카테고리의 활성 아이템 조회 */
    List<RefrigeratorItem> findByUserIdxAndCategoryIdAndIsDeletedFalseOrderByExpirationDateAsc(Integer userId, Long categoryId);

    /** ID로 활성 아이템 조회 (수정/삭제시 사용) */
    Optional<RefrigeratorItem> findByIdAndIsDeletedFalse(Long itemId);

    // =================================================================
    // 검색 및 필터링
    // =================================================================

    /** 유통기한 임박 아이템 조회 (N일 이내) */
    @Query("""
        SELECT ri FROM RefrigeratorItem ri 
        WHERE ri.user.idx = :userId 
        AND ri.isDeleted = false 
        AND ri.expirationDate IS NOT NULL 
        AND ri.expirationDate <= :targetDate
        ORDER BY ri.expirationDate ASC
    """)
    List<RefrigeratorItem> findExpiringItems(@Param("userId") Integer userId, @Param("targetDate") LocalDate targetDate);

    /** 만료된 아이템 조회 */
    @Query("""
        SELECT ri FROM RefrigeratorItem ri 
        WHERE ri.user.idx = :userId 
        AND ri.isDeleted = false 
        AND ri.expirationDate IS NOT NULL 
        AND ri.expirationDate < CURRENT_DATE
        ORDER BY ri.expirationDate DESC
    """)
    List<RefrigeratorItem> findExpiredItems(@Param("userId") Integer userId);
}