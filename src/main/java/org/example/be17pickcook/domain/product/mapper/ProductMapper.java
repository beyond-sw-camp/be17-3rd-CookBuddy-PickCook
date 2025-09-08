package org.example.be17pickcook.domain.product.mapper;

import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.product.model.ProductDto;
import org.mapstruct.*;

import java.util.List;

/**
 * Product Entity ↔ DTO 변환 매퍼 (최소화 버전)
 * - MapStruct 자동 매핑 활용
 * - 실제 사용하는 메서드만 포함
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {

    // =================================================================
    // 카테고리 필터링에서 실제 사용하는 메서드만
    // =================================================================

    /**
     * 여러 Entity → ProductListResponse DTO 리스트 변환
     * - 카테고리별 상품 목록 조회용
     * - isInCart는 Service에서 별도 설정
     */
    @Mapping(target = "isInCart", ignore = true)
    List<ProductDto.ProductListResponse> entityListToProductListResponseList(List<Product> entities);
}