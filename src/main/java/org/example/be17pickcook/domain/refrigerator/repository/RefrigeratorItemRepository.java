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

    /** 사용자별 특정 위치의 활성 아이템 조회 */
    List<RefrigeratorItem> findByUserIdxAndLocationAndIsDeletedFalseOrderByExpirationDateAsc(Integer userId, String location);

    /** 사용자별 특정 카테고리의 활성 아이템 조회 */
    List<RefrigeratorItem> findByUserIdxAndCategoryIdAndIsDeletedFalseOrderByExpirationDateAsc(Integer userId, Long categoryId);

    /** ID로 활성 아이템 조회 (수정/삭제시 사용) */
    Optional<RefrigeratorItem> findByIdAndIsDeletedFalse(Long itemId);

    /** 사용자별 삭제된 아이템 조회 (최근 삭제 순) */
    List<RefrigeratorItem> findByUserIdxAndIsDeletedTrueOrderByDeletedAtDesc(Integer userId);

    // =================================================================
    // 검색 및 필터링
    // =================================================================

    /** 재료명 기반 검색 (LIKE 검색) */
    @Query("""
        SELECT ri FROM RefrigeratorItem ri 
        WHERE ri.user.idx = :userId 
        AND ri.isDeleted = false 
        AND ri.ingredientName LIKE %:keyword%
        ORDER BY ri.location ASC, ri.expirationDate ASC
    """)
    List<RefrigeratorItem> findByUserIdAndIngredientNameContaining(@Param("userId") Integer userId, @Param("keyword") String keyword);

    /** 복합 필터링: 카테고리 + 위치 */
    @Query("""
        SELECT ri FROM RefrigeratorItem ri 
        WHERE ri.user.idx = :userId 
        AND ri.isDeleted = false 
        AND (:categoryId IS NULL OR ri.category.id = :categoryId)
        AND (:location IS NULL OR ri.location = :location)
        ORDER BY ri.location ASC, ri.expirationDate ASC
    """)
    List<RefrigeratorItem> findByUserIdAndFilters(
            @Param("userId") Integer userId,
            @Param("categoryId") Long categoryId,
            @Param("location") String location
    );

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
    // 통계 및 분석용 쿼리
    // =================================================================

    /** 사용자별 카테고리별 아이템 개수 */
    @Query("""
        SELECT ri.category.id, ri.category.name, COUNT(ri.id) 
        FROM RefrigeratorItem ri 
        WHERE ri.user.idx = :userId AND ri.isDeleted = false
        GROUP BY ri.category.id, ri.category.name 
        ORDER BY ri.category.id
    """)
    List<Object[]> countItemsByCategoryForUser(@Param("userId") Integer userId);

    /** 사용자별 위치별 아이템 개수 */
    @Query("""
        SELECT ri.location, COUNT(ri.id) 
        FROM RefrigeratorItem ri 
        WHERE ri.user.idx = :userId AND ri.isDeleted = false
        GROUP BY ri.location 
        ORDER BY ri.location
    """)
    List<Object[]> countItemsByLocationForUser(@Param("userId") Integer userId);

    /** 사용자별 카테고리별 유통기한 임박 아이템 개수 (CategoryService용) */
    @Query("""
        SELECT ri.category.id, COUNT(ri.id) 
        FROM RefrigeratorItem ri 
        WHERE ri.user.idx = :userId 
        AND ri.isDeleted = false 
        AND ri.expirationDate IS NOT NULL 
        AND ri.expirationDate <= :targetDate
        GROUP BY ri.category.id 
        ORDER BY ri.category.id
    """)
    List<Object[]> countExpiringItemsByCategoryForUser(
            @Param("userId") Integer userId,
            @Param("targetDate") LocalDate targetDate
    );

    // =================================================================
    // 추천 시스템용 쿼리 (삭제된 데이터 활용)
    // =================================================================

    /** 사용자별 자주 사용(삭제)된 재료 TOP N */
    @Query("""
        SELECT ri.ingredientName, COUNT(ri.id) as usage_count
        FROM RefrigeratorItem ri 
        WHERE ri.user.idx = :userId 
        AND ri.isDeleted = true 
        AND ri.deletedAt >= :since
        GROUP BY ri.ingredientName 
        ORDER BY usage_count DESC 
        LIMIT :limit
    """)
    List<Object[]> findFrequentlyUsedIngredients(
            @Param("userId") Integer userId,
            @Param("since") LocalDateTime since,
            @Param("limit") int limit
    );

    /** 사용자별 카테고리별 소비 패턴 */
    @Query("""
        SELECT ri.category.id, ri.category.name, COUNT(ri.id) as usage_count
        FROM RefrigeratorItem ri 
        WHERE ri.user.idx = :userId 
        AND ri.isDeleted = true 
        AND ri.deletedAt >= :since
        GROUP BY ri.category.id, ri.category.name 
        ORDER BY usage_count DESC
    """)
    List<Object[]> findConsumptionPatternByCategory(
            @Param("userId") Integer userId,
            @Param("since") LocalDateTime since
    );

    // =================================================================
    // 실행 취소용 쿼리 (최근 삭제된 아이템)
    // =================================================================

    /** 최근 N분 내 삭제된 아이템 조회 (실행 취소 대상) */
    @Query("""
        SELECT ri FROM RefrigeratorItem ri 
        WHERE ri.user.idx = :userId 
        AND ri.isDeleted = true 
        AND ri.deletedAt >= :since
        ORDER BY ri.deletedAt DESC
    """)
    List<RefrigeratorItem> findRecentlyDeletedItems(
            @Param("userId") Integer userId,
            @Param("since") LocalDateTime since
    );
}