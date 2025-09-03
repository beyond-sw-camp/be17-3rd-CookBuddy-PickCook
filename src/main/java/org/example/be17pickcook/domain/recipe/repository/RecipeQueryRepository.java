package org.example.be17pickcook.domain.recipe.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.example.be17pickcook.common.PageResponse;
import org.example.be17pickcook.domain.recipe.model.QRecipe;
import org.example.be17pickcook.domain.recipe.model.RecipeListResponseDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RecipeQueryRepository {

    private final JPAQueryFactory queryFactory;

    public RecipeQueryRepository(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }


    public PageResponse<RecipeListResponseDto> getRecipesFiltered(
            String keyword, List<String> categories, List<String> difficulties, String sortBy, int page, int size) {
        QRecipe recipe = QRecipe.recipe;

         JPQLQuery<RecipeListResponseDto> query = queryFactory
                .select(Projections.constructor(
                        RecipeListResponseDto.class,
                        recipe.idx,
                        recipe.title,
                        recipe.cooking_method,
                        recipe.category,
                        recipe.time_taken,
                        recipe.difficulty_level,
                        recipe.serving_size,
                        recipe.hashtags,
                        recipe.image_large_url,
                        recipe.likeCount,
                        recipe.scrapCount
                ))
                .from(recipe);

        // 검색어 조건
        if (keyword != null && !keyword.isBlank()) {
            query.where(recipe.title.containsIgnoreCase(keyword));
        }

        // 카테고리 조건
        if (categories != null && !categories.isEmpty()) {
            query.where(recipe.category.in(categories));
        }

        // 난이도 조건
        if (difficulties != null && !difficulties.isEmpty()) {
            query.where(recipe.difficulty_level.in(difficulties));
        }

        // 정렬 조건
        if ("popular".equalsIgnoreCase(sortBy)) {
            query.orderBy(recipe.likeCount.desc());
        } else if ("recent".equalsIgnoreCase(sortBy)) {
            query.orderBy(recipe.createdAt.desc());
        } else if ("comments".equalsIgnoreCase(sortBy)) {
            query.orderBy(recipe.commentCount.desc()); // 댓글 개수 기준
        } else {
            query.orderBy(recipe.idx.desc()); // 기본값: 최신순
        }

        // 전체 개수
        long total = query.fetchCount();

        // 페이징
        List<RecipeListResponseDto> results = query
                .offset((long) page * size)
                .limit(size)
                .fetch();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponse<>(results, page, totalPages, total, size);
    }
}
