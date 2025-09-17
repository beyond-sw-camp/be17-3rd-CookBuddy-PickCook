package org.example.be17pickcook.domain.recipe.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.likes.model.QLike;
import org.example.be17pickcook.domain.recipe.model.*;
import org.example.be17pickcook.domain.scrap.model.QScrap;
import org.example.be17pickcook.domain.scrap.model.ScrapTargetType;
import org.example.be17pickcook.domain.user.model.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import com.querydsl.core.types.dsl.BooleanTemplate;


import java.util.List;
import java.util.stream.Collectors;

import static javax.management.Query.or;

@Repository
public class RecipeQueryRepository {

    private final JPAQueryFactory queryFactory;

    public RecipeQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Page<RecipeDto.RecipeListResponseDto> getRecipesFiltered(
            String keyword, int page, int size, String dir, Integer userIdx) {

        QRecipe recipe = QRecipe.recipe;
        QLike likes = QLike.like;
        QScrap scraps = QScrap.scrap;

        // 검색 조건
        BooleanBuilder builder = new BooleanBuilder();
        if (keyword != null && !keyword.isBlank()) {
            builder.and(recipe.title.containsIgnoreCase(keyword)
                    .or(recipe.description.containsIgnoreCase(keyword)));
        }

        // 데이터 조회
        List<RecipeDto.RecipeListResponseDto> content = queryFactory
                .select(Projections.constructor(
                        RecipeDto.RecipeListResponseDto.class,
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
                        recipe.scrapCount,
                        recipe.description,
                        likes.idx.isNotNull(),
                        scraps.idx.isNotNull()
                ))
                .from(recipe)
                .leftJoin(likes)
                    .on(likes.targetId.eq(recipe.idx)
                            .and(likes.targetType.eq(LikeTargetType.RECIPE))
                            .and(likes.user.idx.eq(userIdx)))
                .leftJoin(scraps)
                    .on(scraps.targetId.eq(recipe.idx)
                            .and(scraps.targetType.eq(ScrapTargetType.RECIPE))
                            .and(scraps.user.idx.eq(userIdx)))
                .where(builder)
                .groupBy(recipe.idx)
                .orderBy("DESC".equalsIgnoreCase(dir) ? recipe.createdAt.desc() : recipe.createdAt.asc())
                .offset(page * size)
                .limit(size)
                .fetch();

        // 전체 건수 조회
        Long total = queryFactory
                .select(recipe.count())
                .from(recipe)
                .where(builder)
                .fetchOne();

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }
}