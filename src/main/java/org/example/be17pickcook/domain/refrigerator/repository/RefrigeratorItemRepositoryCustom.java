package org.example.be17pickcook.domain.refrigerator.repository;

import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItem;
import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItemDto;

import java.time.LocalDate;
import java.util.List;

/**
 * 냉장고 아이템 커스텀 리포지토리 인터페이스
 * - QueryDSL을 사용한 동적 쿼리 처리
 */
public interface RefrigeratorItemRepositoryCustom {

    /**
     * 복합 필터링으로 냉장고 아이템 조회 (QueryDSL 버전)
     */
    List<RefrigeratorItem> findByComplexFilterWithQueryDsl(
            Integer userId,
            String keyword,
            Long categoryId,
            RefrigeratorItemDto.ExpirationStatus expirationStatus,
            RefrigeratorItemDto.SortType sortType,
            RefrigeratorItemDto.SortDirection sortDirection
    );
}