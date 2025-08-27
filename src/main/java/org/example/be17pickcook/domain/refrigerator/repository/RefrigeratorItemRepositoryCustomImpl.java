package org.example.be17pickcook.domain.refrigerator.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItem;
import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItemDto;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static org.example.be17pickcook.domain.refrigerator.model.QRefrigeratorItem.refrigeratorItem;

/**
 * 냉장고 아이템 커스텀 리포지토리 구현체
 * - QueryDSL을 활용한 동적 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class RefrigeratorItemRepositoryCustomImpl implements RefrigeratorItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<RefrigeratorItem> findByComplexFilterWithQueryDsl(
            Integer userId,
            String keyword,
            Long categoryId,
            RefrigeratorItemDto.ExpirationStatus expirationStatus,
            RefrigeratorItemDto.SortType sortType,
            RefrigeratorItemDto.SortDirection sortDirection) {

        return queryFactory
                .selectFrom(refrigeratorItem)
                .where(
                        refrigeratorItem.user.idx.eq(userId),
                        refrigeratorItem.isDeleted.eq(false),
                        keywordCondition(keyword),
                        categoryCondition(categoryId),
                        expirationStatusCondition(expirationStatus)
                )
                .orderBy(createOrderSpecifier(sortType, sortDirection))
                .fetch();
    }

    // =================================================================
    // 조건별 private 메서드들
    // =================================================================

    /**
     * 키워드 검색 조건
     */
    private BooleanExpression keywordCondition(String keyword) {
        return keyword != null && !keyword.trim().isEmpty()
                ? refrigeratorItem.ingredientName.containsIgnoreCase(keyword.trim())
                : null;
    }

    /**
     * 카테고리 필터 조건
     */
    private BooleanExpression categoryCondition(Long categoryId) {
        return categoryId != null ? refrigeratorItem.category.id.eq(categoryId) : null;
    }

    /**
     * 유통기한 상태별 필터 조건
     */
    private BooleanExpression expirationStatusCondition(RefrigeratorItemDto.ExpirationStatus expirationStatus) {
        if (expirationStatus == null) {
            return null;
        }

        LocalDate today = LocalDate.now();

        return switch (expirationStatus) {
            case FRESH -> refrigeratorItem.expirationDate.goe(today.plusDays(4));
            case EXPIRING_SOON -> refrigeratorItem.expirationDate.goe(today.plusDays(2))
                    .and(refrigeratorItem.expirationDate.lt(today.plusDays(4)));
            case URGENT -> refrigeratorItem.expirationDate.goe(today)
                    .and(refrigeratorItem.expirationDate.lt(today.plusDays(2)));
            case EXPIRED -> refrigeratorItem.expirationDate.lt(today);
        };
    }

    /**
     * 정렬 조건 생성
     */
    private OrderSpecifier<?> createOrderSpecifier(
            RefrigeratorItemDto.SortType sortType,
            RefrigeratorItemDto.SortDirection sortDirection) {

        if (sortType == null) {
            return refrigeratorItem.expirationDate.asc(); // 기본 정렬
        }

        return switch (sortType) {
            case EXPIRATION_DATE -> sortDirection == RefrigeratorItemDto.SortDirection.DESC
                    ? refrigeratorItem.expirationDate.desc()
                    : refrigeratorItem.expirationDate.asc();
            case CREATED_DATE -> sortDirection == RefrigeratorItemDto.SortDirection.DESC
                    ? refrigeratorItem.createdAt.desc()
                    : refrigeratorItem.createdAt.asc();
        };
    }
}