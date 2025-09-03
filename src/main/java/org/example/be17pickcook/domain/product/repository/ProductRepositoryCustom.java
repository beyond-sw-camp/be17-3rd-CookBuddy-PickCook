package org.example.be17pickcook.domain.product.repository;

import org.example.be17pickcook.domain.product.model.ProductDto;

import java.util.List;

/**
 * 상품 커스텀 리포지토리 인터페이스
 * - QueryDSL을 사용한 레시피 기반 연관 상품 조회
 */
public interface ProductRepositoryCustom {

    /**
     * 레시피 재료 기반 연관 상품 조회
     * @param recipeId 레시피 ID
     * @param limit 조회 개수 제한 (기본 16개)
     * @return 연관 상품 목록
     */
    List<ProductDto.RelatedProductResponse> findProductsByRecipeIngredients(Long recipeId, Integer limit);

    /**
     * 랜덤 상품 조회 (매칭 상품 부족 시 사용)
     * @param limit 조회 개수
     * @return 랜덤 상품 목록
     */
    List<ProductDto.RelatedProductResponse> findRandomProducts(Integer limit);
}