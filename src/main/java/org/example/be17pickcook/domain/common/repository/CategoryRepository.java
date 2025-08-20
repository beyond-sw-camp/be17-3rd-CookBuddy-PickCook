package org.example.be17pickcook.domain.common.repository;

import org.example.be17pickcook.domain.common.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 리포지토리 (간소화 버전)
 * - 기본 CRUD 및 필요한 커스텀 쿼리만 제공
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // =================================================================
    // 기본 조회 메서드
    // =================================================================

    /** 모든 카테고리를 ID 순서대로 조회 (표시 순서) */
    List<Category> findAllByOrderById();

    /** 카테고리명으로 조회 */
    Optional<Category> findByName(String name);

    /** 카테고리명 중복 확인 */
    boolean existsByName(String name);

    // =================================================================
    // 사용자별 활성 카테고리 조회 (냉장고 아이템 기준)
    // =================================================================

    /** 특정 사용자가 실제 사용 중인 카테고리 조회 */
    @Query("""
        SELECT DISTINCT c 
        FROM Category c 
        INNER JOIN RefrigeratorItem ri ON ri.category.id = c.id 
        WHERE ri.user.idx = :userId AND ri.isDeleted = false
        ORDER BY c.id
    """)
    List<Category> findActiveCategoriesByUserId(Integer userId);

    /** 냉장고 아이템이 있는 모든 카테고리 조회 (전체 사용자 기준) */
    @Query("""
        SELECT DISTINCT c 
        FROM Category c 
        INNER JOIN RefrigeratorItem ri ON ri.category.id = c.id 
        WHERE ri.isDeleted = false
        ORDER BY c.id
    """)
    List<Category> findActiveCategoriesGlobal();
}