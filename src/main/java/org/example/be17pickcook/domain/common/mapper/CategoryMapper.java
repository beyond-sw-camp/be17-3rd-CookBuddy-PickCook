package org.example.be17pickcook.domain.common.mapper;

import org.example.be17pickcook.domain.common.model.Category;
import org.example.be17pickcook.domain.common.model.CategoryDto;
import org.mapstruct.*;

import java.util.List;

/**
 * Category Entity ↔ DTO 변환 매퍼
 * - MapStruct 자동 매핑 활용
 * - 단순한 매핑이므로 추가 로직 없음
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CategoryMapper {

    // =================================================================
    // Entity → Response DTO 매핑
    // =================================================================

    /**
     * Category Entity → Response DTO 변환
     */
    CategoryDto.Response entityToResponse(Category entity);

    /**
     * 여러 Entity → Response DTO 리스트 변환
     */
    List<CategoryDto.Response> entityListToResponseList(List<Category> entities);

    // =================================================================
    // Entity → Summary DTO 매핑 (통계 정보용)
    // =================================================================

    /**
     * Category Entity → Summary DTO 변환
     * - itemCount, expiringItemCount는 Service에서 별도 설정
     */
    @Mapping(target = "itemCount", ignore = true)
    @Mapping(target = "expiringItemCount", ignore = true)
    CategoryDto.Summary entityToSummary(Category entity);

    // =================================================================
    // Request DTO → Entity 매핑 (관리자용 - 향후 확장)
    // =================================================================

    /**
     * Request DTO → Category Entity 변환 (추가용)
     * - id는 자동 생성되므로 무시
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refrigeratorItems", ignore = true)
    Category requestToEntity(CategoryDto.Request dto);

    // =================================================================
    // Update DTO → Entity 매핑 (관리자용 - 향후 확장)
    // =================================================================

    /**
     * Update DTO → 기존 Entity 업데이트
     * - null 값은 무시하여 부분 업데이트 지원
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refrigeratorItems", ignore = true)
    void updateEntityFromDto(@MappingTarget Category entity, CategoryDto.Update dto);
}