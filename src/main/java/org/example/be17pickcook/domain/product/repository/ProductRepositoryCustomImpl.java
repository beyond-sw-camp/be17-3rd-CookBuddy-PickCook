package org.example.be17pickcook.domain.product.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.product.model.ProductDto;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.be17pickcook.domain.product.model.QProduct.product;
import static org.example.be17pickcook.domain.recipe.model.QRecipe.recipe;
import static org.example.be17pickcook.domain.recipe.model.QRecipeIngredient.recipeIngredient;

/**
 * 상품 커스텀 리포지토리 구현체
 * - QueryDSL을 활용한 레시피 기반 연관 상품 검색
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductDto.RelatedProductResponse> findProductsByRecipeIngredients(Long recipeId, Integer limit) {
        // 1단계: 해당 레시피의 재료명들 조회
        List<String> ingredientNames = queryFactory
                .select(recipeIngredient.ingredient_name)
                .from(recipeIngredient)
                .join(recipeIngredient.recipe, recipe)
                .where(recipe.idx.eq(recipeId))
                .fetch();

        System.out.println("🔍 레시피 재료들: " + ingredientNames);

        if (ingredientNames.isEmpty()) {
            return findRandomProducts(limit);
        }

        // 2단계: 재료별로 매칭되는 상품들 조회
        List<ProductDto.RelatedProductResponse> matchedProducts = new ArrayList<>();

        for (String ingredientName : ingredientNames) {
            if (ingredientName.length() <= 1) continue; // 1글자 제외

            // 해당 재료와 매칭되는 상품들 조회
            List<ProductDto.RelatedProductResponse> products = queryFactory
                    .select(Projections.constructor(ProductDto.RelatedProductResponse.class,
                            product.id,
                            product.title,
                            product.subtitle,
                            product.original_price,
                            product.discount_rate,
                            product.main_image_url,
                            product.category,
                            Expressions.constant("INGREDIENT_MATCH"),
                            Expressions.constant(ingredientName)
                    ))
                    .from(product)
                    .where(createRelaxedMatchCondition(ingredientName))
                    .limit(4) // 재료당 최대 4개
                    .fetch();

            System.out.println("🔍 재료 '" + ingredientName + "'로 찾은 상품 수: " + products.size());

            matchedProducts.addAll(products);

            if (matchedProducts.size() >= limit) break;
        }

        // 3단계: 부족하면 랜덤 상품으로 보충
        if (matchedProducts.size() < limit) {
            int remainingCount = limit - matchedProducts.size();
            List<ProductDto.RelatedProductResponse> randomProducts = findRandomProducts(remainingCount);
            matchedProducts.addAll(randomProducts);
        }

        return matchedProducts.stream()
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto.RelatedProductResponse> findRandomProducts(Integer limit) {
        return queryFactory
                .select(Projections.constructor(ProductDto.RelatedProductResponse.class,
                        product.id,
                        product.title,
                        product.subtitle,
                        product.original_price,
                        product.discount_rate,
                        product.main_image_url,
                        product.category,
                        Expressions.constant("RANDOM"),
                        Expressions.constant("")
                ))
                .from(product)
                .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
                .limit(limit)
                .fetch();
    }

    /**
     * 완화된 매칭 조건 (연관단어 검색)
     */
    private BooleanExpression createRelaxedMatchCondition(String ingredientName) {
        BooleanExpression condition = null;

        // 1순위: 전체 단어 매칭
        condition = product.title.containsIgnoreCase(ingredientName)
                .or(product.subtitle.containsIgnoreCase(ingredientName));

        // 2순위: 키워드 토큰 매칭 (2글자 이상 단어에 대해)
        if (ingredientName.length() >= 3) {
            List<String> tokens = extractKeywordTokens(ingredientName);
            for (String token : tokens) {
                if (token.length() >= 2) { // 2글자 이상 토큰만
                    condition = condition.or(
                            product.title.containsIgnoreCase(token)
                                    .or(product.subtitle.containsIgnoreCase(token))
                    );
                }
            }
        }

        return condition;
    }

    /**
     * 키워드에서 토큰 추출
     */
    private List<String> extractKeywordTokens(String keyword) {
        List<String> tokens = new ArrayList<>();

        // 기본 전략들
        if (keyword.length() >= 3) {
            // 앞 2글자, 뒤 2글자
            if (keyword.length() >= 4) {
                tokens.add(keyword.substring(0, 2));
                tokens.add(keyword.substring(keyword.length() - 2));
            }

            // 특정 단어 패턴 처리
            tokens.addAll(extractFoodRelatedTokens(keyword));
        }

        return tokens.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 음식 관련 특수 토큰 추출
     */
    private List<String> extractFoodRelatedTokens(String keyword) {
        List<String> tokens = new ArrayList<>();

        // 고기류 패턴
        if (keyword.contains("고기")) {
            tokens.add("고기");
            String prefix = keyword.replace("고기", "").trim();
            if (!prefix.isEmpty() && prefix.length() >= 2) {
                tokens.add(prefix);
            }
        }

        // 생선류 패턴
        if (keyword.contains("생선") || keyword.contains("물고기")) {
            tokens.add("생선");
            tokens.add("물고기");
        }

        // 채소류 패턴
        if (keyword.endsWith("채") || keyword.endsWith("배추")) {
            tokens.add("채소");
            if (keyword.contains("배추")) tokens.add("배추");
        }

        // 특정 재료별 연관단어
        switch (keyword) {
            case "양파":
                tokens.addAll(List.of("양파", "파"));
                break;
            case "대파":
                tokens.addAll(List.of("파", "대파"));
                break;
            case "쪽파":
                tokens.addAll(List.of("파", "쪽파"));
                break;
            case "돼지고기":
                tokens.addAll(List.of("돼지", "고기", "삼겹살", "목살"));
                break;
            case "쇠고기":
                tokens.addAll(List.of("소고기", "고기", "불고기", "갈비"));
                break;
            case "닭고기":
                tokens.addAll(List.of("닭", "고기", "치킨"));
                break;
            case "달걀":
            case "계란":
                tokens.addAll(List.of("달걀", "계란"));
                break;
        }

        return tokens;
    }
}