package org.example.be17pickcook.domain.recipe.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
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
            Integer userIdx, String keyword, int page, int size, String sortType, String difficulty, String category, String cookingMethod ) {

        QRecipe recipe = QRecipe.recipe;
        QLike likes = QLike.like;
        QScrap scraps = QScrap.scrap;

        // 검색 조건
        BooleanBuilder builder = new BooleanBuilder();
        if (keyword != null && !keyword.isBlank()) {
            builder.and(recipe.title.containsIgnoreCase(keyword)
                    .or(recipe.description.containsIgnoreCase(keyword)));
        }

        // 난이도 필터
        if (difficulty != null && !difficulty.isBlank()) {
            builder.and(recipe.difficulty_level.eq(difficulty));
        }

        // 카테고리 필터
        if (category != null && !category.isBlank()) {
            builder.and(recipe.category.eq(category));
        }

        // 조리방법 필터
        if (cookingMethod != null && !cookingMethod.isBlank()) {
            builder.and(recipe.cooking_method.eq(cookingMethod));
        }

        // 정렬 조건
        OrderSpecifier<?> orderSpecifier = switch (sortType) {
            case "oldest" -> recipe.createdAt.asc();
            case "likes" -> recipe.likeCount.desc();
            case "scraps" -> recipe.scrapCount.desc();
            default -> recipe.createdAt.desc();
        };

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
                .orderBy(orderSpecifier)
                .offset((long) page * size)
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

    // 📍 위치: RecipeQueryRepository.java에 새 메서드 추가 (기존 getRecipesFiltered 메서드 아래)

    public Page<RecipeDto.RecipeListResponseDto> getRecipesWithFilter(
            int page, int size, String sortType, String difficulty,
            String category, String cookingMethod, Integer userIdx) {

        QRecipe recipe = QRecipe.recipe;
        QLike likes = QLike.like;
        QScrap scraps = QScrap.scrap;

        // 1. 동적 필터 조건 구성
        BooleanBuilder builder = new BooleanBuilder();

        // 난이도 필터
        if (difficulty != null && !difficulty.isBlank()) {
            builder.and(recipe.difficulty_level.eq(difficulty));
        }

        // 카테고리 필터
        if (category != null && !category.isBlank()) {
            builder.and(recipe.category.eq(category));
        }

        // 조리방법 필터
        if (cookingMethod != null && !cookingMethod.isBlank()) {
            builder.and(recipe.cooking_method.eq(cookingMethod));
        }

        // 2. 정렬 조건 설정
        OrderSpecifier<?> orderSpecifier = switch (sortType) {
            case "oldest" -> recipe.createdAt.asc();
            case "likes" -> recipe.likeCount.desc();
            case "scraps" -> recipe.scrapCount.desc();
            default -> recipe.createdAt.desc(); // latest
        };

        // 3. 데이터 조회
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
                        Expressions.constant(false), // likedByUser
                        Expressions.constant(false)  // scrappedByUser
                ))
                .from(recipe)
                .where(builder)
                .orderBy(orderSpecifier)
                .offset((long) page * size)
                .limit(size)
                .fetch();

        // 4. 전체 건수 조회
        Long total = queryFactory
                .select(recipe.count())
                .from(recipe)
                .where(builder)
                .fetchOne();

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }
}