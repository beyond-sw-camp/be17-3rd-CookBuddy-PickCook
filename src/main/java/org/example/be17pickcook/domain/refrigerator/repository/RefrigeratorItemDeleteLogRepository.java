package org.example.be17pickcook.domain.refrigerator.repository;

import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItemDeleteLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 냉장고 아이템 삭제 로그 리포지토리
 */
public interface RefrigeratorItemDeleteLogRepository extends JpaRepository<RefrigeratorItemDeleteLog, Long> {

    /**
     * 사용자별 자주 삭제하는 재료 TOP 10 (최근 6개월)
     */
    @Query("""
        SELECT dl.ingredientName, COUNT(dl) as deleteCount
        FROM RefrigeratorItemDeleteLog dl 
        WHERE dl.user.idx = :userId 
        AND dl.deletedDate >= :fromDate
        GROUP BY dl.ingredientName
        ORDER BY COUNT(dl) DESC
        LIMIT 10
    """)
    List<Object[]> findTopDeletedIngredientsByUser(@Param("userId") Integer userId, @Param("fromDate") LocalDate fromDate);

    /**
     * 사용자별 평균 보관 기간 (카테고리별)
     */
    @Query("""
        SELECT dl.category.name, AVG(dl.storageDays) as avgDays
        FROM RefrigeratorItemDeleteLog dl 
        WHERE dl.user.idx = :userId 
        AND dl.deletedDate >= :fromDate
        GROUP BY dl.category.id
    """)
    List<Object[]> findAverageStorageDaysByCategory(@Param("userId") Integer userId, @Param("fromDate") LocalDate fromDate);
}