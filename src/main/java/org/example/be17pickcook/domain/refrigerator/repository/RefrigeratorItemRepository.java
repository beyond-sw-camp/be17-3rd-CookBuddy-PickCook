package org.example.be17pickcook.domain.refrigerator.repository;

import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 냉장고 아이템 리포지토리
 * - 소프트 삭제 지원
 * - 검색, 필터링, 정렬 기능
 */
public interface RefrigeratorItemRepository extends JpaRepository<RefrigeratorItem, Long> {

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

    // =================================================================
    // 개선된 복합 필터링 (유통기한 상태를 DB에서 처리) - 새로 추가
    // =================================================================

    /**
     * 복합 필터링 쿼리 (키워드 + 카테고리 + 유통기한 상태 + 동적 정렬)
     */
    @Query(value = """
    SELECT ri FROM RefrigeratorItem ri 
    WHERE ri.user.idx = :userId 
    AND ri.isDeleted = false
    AND (:keyword IS NULL OR ri.ingredientName LIKE %:keyword%)
    AND (:categoryId IS NULL OR ri.category.id = :categoryId)
    AND (
        :expirationStatus IS NULL OR
        (:expirationStatus = 'FRESH' AND ri.expirationDate >= :freshDate) OR
        (:expirationStatus = 'EXPIRING_SOON' AND ri.expirationDate >= :soonStartDate AND ri.expirationDate < :soonEndDate) OR
        (:expirationStatus = 'URGENT' AND ri.expirationDate >= :urgentStartDate AND ri.expirationDate < :urgentEndDate) OR
        (:expirationStatus = 'EXPIRED' AND ri.expirationDate < :today)
    )
    ORDER BY 
        CASE WHEN :sortType = 'EXPIRATION_DATE' AND :sortDirection = 'ASC' THEN ri.expirationDate END ASC,
        CASE WHEN :sortType = 'EXPIRATION_DATE' AND :sortDirection = 'DESC' THEN ri.expirationDate END DESC,
        CASE WHEN :sortType = 'CREATED_DATE' AND :sortDirection = 'ASC' THEN ri.createdAt END ASC,
        CASE WHEN :sortType = 'CREATED_DATE' AND :sortDirection = 'DESC' THEN ri.createdAt END DESC
    """)
    List<RefrigeratorItem> findByComplexFilter(
            @Param("userId") Integer userId,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("expirationStatus") String expirationStatus,
            @Param("sortType") String sortType,
            @Param("sortDirection") String sortDirection,
            @Param("today") LocalDate today,
            @Param("freshDate") LocalDate freshDate,
            @Param("soonStartDate") LocalDate soonStartDate,
            @Param("soonEndDate") LocalDate soonEndDate,
            @Param("urgentStartDate") LocalDate urgentStartDate,
            @Param("urgentEndDate") LocalDate urgentEndDate
    );
}