package org.example.be17pickcook.domain.refrigerator.mapper;

import org.example.be17pickcook.domain.common.mapper.CategoryMapper;
import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItem;
import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItemDto;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * RefrigeratorItem Entity ↔ DTO 변환 매퍼
 * - MapStruct 자동 매핑 활용
 * - CategoryMapper 의존성 주입
 * - 유통기한 상태 계산 로직 포함
 */
@Mapper(
        componentModel = "spring",
        uses = {CategoryMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RefrigeratorItemMapper {

    // =================================================================
    // Entity → Response DTO 매핑
    // =================================================================

    /**
     * RefrigeratorItem Entity → Response DTO 변환
     * - 유통기한 상태 및 남은 일수 자동 계산
     * - CategoryMapper를 통해 카테고리 정보 매핑
     */
    @Mapping(target = "expirationStatus", expression = "java(calculateExpirationStatus(entity.getExpirationDate()))")
    @Mapping(target = "daysUntilExpiration", expression = "java(calculateDaysUntilExpiration(entity.getExpirationDate()))")
    RefrigeratorItemDto.Response entityToResponse(RefrigeratorItem entity);

    /**
     * 여러 Entity → Response DTO 리스트 변환
     */
    List<RefrigeratorItemDto.Response> entityListToResponseList(List<RefrigeratorItem> entities);

    // =================================================================
    // Request DTO → Entity 매핑 (추가)
    // =================================================================

    /**
     * Request DTO → RefrigeratorItem Entity 변환 (추가용)
     * - user, id, 시간 필드, 소프트 삭제 필드는 Service에서 설정
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)  // Service에서 Category 엔티티 설정
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    RefrigeratorItem requestToEntity(RefrigeratorItemDto.Request dto);

    // =================================================================
    // Update DTO → Entity 매핑 (수정)
    // =================================================================

    /**
     * Update DTO → 기존 Entity 업데이트
     * - null 값은 무시하여 부분 업데이트 지원
     * - id, user, 시간 필드는 변경 불가
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)  // Service에서 카테고리 변경 처리
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDto(@MappingTarget RefrigeratorItem entity, RefrigeratorItemDto.Update dto);

    // =================================================================
    // 유통기한 계산 로직 (default 메서드)
    // =================================================================

    /**
     * 유통기한 상태 계산
     * @param expirationDate 유통기한
     * @return 상태 (FRESH/EXPIRING_SOON/URGENT/EXPIRED)
     */
    default RefrigeratorItemDto.ExpirationStatus calculateExpirationStatus(LocalDate expirationDate) {
        if (expirationDate == null) {
            return RefrigeratorItemDto.ExpirationStatus.FRESH;
        }

        LocalDate today = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(today, expirationDate);

        if (daysUntil < 0) {
            return RefrigeratorItemDto.ExpirationStatus.EXPIRED;    // 유통기한 지남
        } else if (daysUntil <= 1) {
            return RefrigeratorItemDto.ExpirationStatus.URGENT;     // 1일 이하 남음
        } else if (daysUntil <= 3) {
            return RefrigeratorItemDto.ExpirationStatus.EXPIRING_SOON; // 2-3일 남음
        } else {
            return RefrigeratorItemDto.ExpirationStatus.FRESH;      // 4일 이상 남음
        }
    }

    /**
     * 유통기한까지 남은 일수 계산
     * @param expirationDate 유통기한
     * @return 남은 일수 (음수면 만료된 일수)
     */
    default Integer calculateDaysUntilExpiration(LocalDate expirationDate) {
        if (expirationDate == null) {
            return null;  // 유통기한 미설정 시 null 반환
        }

        LocalDate today = LocalDate.now();
        return (int) ChronoUnit.DAYS.between(today, expirationDate);
    }
}