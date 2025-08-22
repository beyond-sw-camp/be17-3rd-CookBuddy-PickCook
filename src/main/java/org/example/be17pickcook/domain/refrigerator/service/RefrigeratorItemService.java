package org.example.be17pickcook.domain.refrigerator.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.exception.BaseException;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.domain.common.model.Category;
import org.example.be17pickcook.domain.common.repository.CategoryRepository;
import org.example.be17pickcook.domain.refrigerator.mapper.RefrigeratorItemMapper;
import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItem;
import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItemDto;
import org.example.be17pickcook.domain.refrigerator.repository.RefrigeratorItemRepository;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 냉장고 아이템 서비스
 * - 식재료 CRUD 관리
 * - 소프트 삭제 및 복원 기능
 * - 검색, 필터링, 통계 기능
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefrigeratorItemService {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final RefrigeratorItemRepository refrigeratorItemRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RefrigeratorItemMapper refrigeratorItemMapper;

    // =================================================================
    // 기본 CRUD 관련 API
    // =================================================================

    /**
     * 냉장고 아이템 추가
     */
    @Transactional
    public RefrigeratorItemDto.Response create(RefrigeratorItemDto.Request dto, Integer userId) {
        // 사용자 존재 확인
        User user = findUserById(userId);

        // 카테고리 존재 확인
        Category category = findCategoryById(dto.getCategoryId());

        // DTO → Entity 변환
        RefrigeratorItem entity = refrigeratorItemMapper.requestToEntity(dto);
        entity = entity.toBuilder()
                .user(user)
                .category(category)
                .build();

        // 저장
        RefrigeratorItem savedEntity = refrigeratorItemRepository.save(entity);

        return refrigeratorItemMapper.entityToResponse(savedEntity);
    }

    /**
     * 사용자별 냉장고 아이템 전체 조회 (위치별 정렬)
     */
    public List<RefrigeratorItemDto.Response> findByUserId(Integer userId) {
        validateUserExists(userId);

        List<RefrigeratorItem> items = refrigeratorItemRepository
                .findByUserIdxAndIsDeletedFalseOrderByLocationAscExpirationDateAsc(userId);

        return refrigeratorItemMapper.entityListToResponseList(items);
    }

    /**
     * 특정 냉장고 아이템 조회
     */
    public RefrigeratorItemDto.Response findById(Long itemId, Integer userId) {
        RefrigeratorItem item = findActiveItemByIdAndUserId(itemId, userId);
        return refrigeratorItemMapper.entityToResponse(item);
    }

    /**
     * 냉장고 아이템 수정
     */
    @Transactional
    public RefrigeratorItemDto.Response update(Long itemId, RefrigeratorItemDto.Update dto, Integer userId) {
        // 기존 아이템 조회
        RefrigeratorItem existingItem = findActiveItemByIdAndUserId(itemId, userId);

        // 카테고리 변경 시 검증
        if (dto.getCategoryId() != null) {
            Category newCategory = findCategoryById(dto.getCategoryId());
            existingItem = existingItem.toBuilder()
                    .category(newCategory)
                    .build();
        }

        // DTO 정보로 Entity 업데이트
        refrigeratorItemMapper.updateEntityFromDto(existingItem, dto);

        // 도메인 메서드로 비즈니스 규칙 적용
        if (dto.getIngredientName() != null) {
            existingItem.changeIngredientName(dto.getIngredientName());
        }
        if (dto.getQuantity() != null) {
            existingItem.changeQuantity(dto.getQuantity());
        }
        if (dto.getExpirationDate() != null) {
            existingItem.changeExpirationDate(dto.getExpirationDate());
        }
        if (dto.getLocation() != null) {
            existingItem.changeLocation(dto.getLocation());
        }

        // 저장
        RefrigeratorItem updatedItem = refrigeratorItemRepository.save(existingItem);

        return refrigeratorItemMapper.entityToResponse(updatedItem);
    }

    /**
     * 냉장고 아이템 삭제 (소프트 삭제)
     */
    @Transactional
    public void delete(Long itemId, Integer userId) {
        RefrigeratorItem item = findActiveItemByIdAndUserId(itemId, userId);

        // 소프트 삭제 처리
        item.markAsDeleted();

        refrigeratorItemRepository.save(item);
    }

    /**
     * 삭제된 아이템 복원 (실행 취소)
     */
    @Transactional
    public void undoDelete(Long itemId, Integer userId) {
        // 삭제된 아이템 조회
        RefrigeratorItem item = refrigeratorItemRepository.findById(itemId)
                .filter(i -> i.getUser().getIdx().equals(userId))
                .filter(RefrigeratorItem::getIsDeleted)
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.RESOURCE_NOT_FOUND));

        // 복원 처리
        item.restoreFromDeleted();

        refrigeratorItemRepository.save(item);
    }

    // =================================================================
    // 검색 관련 API
    // =================================================================

    /**
     * 재료명 키워드 검색
     */
    public List<RefrigeratorItemDto.Response> searchByKeyword(String keyword, Integer userId) {
        validateUserExists(userId);

        if (keyword == null || keyword.trim().isEmpty()) {
            return findByUserId(userId);
        }

        List<RefrigeratorItem> items = refrigeratorItemRepository
                .findByUserIdAndIngredientNameContaining(userId, keyword.trim());

        return refrigeratorItemMapper.entityListToResponseList(items);
    }

    /**
     * 복합 필터링 조회
     */
    public List<RefrigeratorItemDto.Response> findByFilters(RefrigeratorItemDto.Filter filter, Integer userId) {
        validateUserExists(userId);

        // 키워드 검색
        if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
            return searchByKeyword(filter.getKeyword(), userId);
        }

        // 카테고리 + 위치 필터링
        List<RefrigeratorItem> items = refrigeratorItemRepository
                .findByUserIdAndFilters(userId, filter.getCategoryId(), filter.getLocation());

        List<RefrigeratorItemDto.Response> responses = refrigeratorItemMapper.entityListToResponseList(items);

        // 유통기한 상태 필터링 (메모리에서 처리)
        if (filter.getExpirationStatus() != null) {
            responses = responses.stream()
                    .filter(item -> filter.getExpirationStatus().equals(item.getExpirationStatus()))
                    .collect(Collectors.toList());
        }

        // 정렬 적용
        return applySorting(responses, filter.getSortType(), filter.getSortDirection());
    }

    // =================================================================
    // 필터링 관련 API
    // =================================================================

    /**
     * 특정 위치의 아이템 조회
     */
    public List<RefrigeratorItemDto.Response> findByLocation(String location, Integer userId) {
        validateUserExists(userId);

        List<RefrigeratorItem> items = refrigeratorItemRepository
                .findByUserIdxAndLocationAndIsDeletedFalseOrderByExpirationDateAsc(userId, location);

        return refrigeratorItemMapper.entityListToResponseList(items);
    }

    /**
     * 특정 카테고리의 아이템 조회
     */
    public List<RefrigeratorItemDto.Response> findByCategory(Long categoryId, Integer userId) {
        validateUserExists(userId);
        validateCategoryExists(categoryId);

        List<RefrigeratorItem> items = refrigeratorItemRepository
                .findByUserIdxAndCategoryIdAndIsDeletedFalseOrderByExpirationDateAsc(userId, categoryId);

        return refrigeratorItemMapper.entityListToResponseList(items);
    }

    // =================================================================
    // 기타 비즈니스 로직 API
    // =================================================================

    /**
     * 일괄 등록 (구매 → 냉장고 등록)
     */
    @Transactional
    public List<RefrigeratorItemDto.Response> createBulk(RefrigeratorItemDto.BulkRequest dto, Integer userId) {
        User user = findUserById(userId);

        return dto.getItems().stream()
                .map(itemDto -> {
                    Category category = findCategoryById(itemDto.getCategoryId());

                    RefrigeratorItem entity = refrigeratorItemMapper.requestToEntity(itemDto);
                    entity = entity.toBuilder()
                            .user(user)
                            .category(category)
                            .build();

                    RefrigeratorItem savedEntity = refrigeratorItemRepository.save(entity);
                    return refrigeratorItemMapper.entityToResponse(savedEntity);
                })
                .collect(Collectors.toList());
    }

    /**
     * 유통기한 임박 아이템 조회
     */
    public List<RefrigeratorItemDto.Response> findExpiringItems(Integer userId, int days) {
        validateUserExists(userId);

        LocalDate targetDate = LocalDate.now().plusDays(days);

        List<RefrigeratorItem> items = refrigeratorItemRepository
                .findExpiringItems(userId, targetDate);

        return refrigeratorItemMapper.entityListToResponseList(items);
    }

    /**
     * 만료된 아이템 조회
     */
    public List<RefrigeratorItemDto.Response> findExpiredItems(Integer userId) {
        validateUserExists(userId);

        List<RefrigeratorItem> items = refrigeratorItemRepository.findExpiredItems(userId);

        return refrigeratorItemMapper.entityListToResponseList(items);
    }

    // =================================================================
    // 유틸리티 메서드들
    // =================================================================

    /**
     * 사용자 ID로 User 엔티티 조회
     */
    private User findUserById(Integer userId) {
        return userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.USER_NOT_FOUND));
    }

    /**
     * 사용자 존재 확인
     */
    private void validateUserExists(Integer userId) {
        if (!userRepository.existsByIdAndNotDeleted(userId)) {
            throw BaseException.from(BaseResponseStatus.USER_NOT_FOUND);
        }
    }

    /**
     * 카테고리 ID로 Category 엔티티 조회
     */
    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.CATEGORY_NOT_FOUND));
    }

    /**
     * 카테고리 존재 확인
     */
    private void validateCategoryExists(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw BaseException.from(BaseResponseStatus.CATEGORY_NOT_FOUND);
        }
    }

    /**
     * 활성 아이템 조회 (소유자 확인 포함)
     */
    private RefrigeratorItem findActiveItemByIdAndUserId(Long itemId, Integer userId) {
        return refrigeratorItemRepository.findByIdAndIsDeletedFalse(itemId)
                .filter(item -> item.getUser().getIdx().equals(userId))
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.RESOURCE_NOT_FOUND));
    }

    /**
     * 정렬 적용
     */
    private List<RefrigeratorItemDto.Response> applySorting(
            List<RefrigeratorItemDto.Response> items,
            RefrigeratorItemDto.SortType sortType,
            RefrigeratorItemDto.SortDirection direction) {

        if (sortType == null) {
            return items;
        }

        return items.stream()
                .sorted((a, b) -> {
                    int comparison = switch (sortType) {
                        case EXPIRATION_DATE -> compareExpirationDate(a, b);
                        case CREATED_DATE -> a.getCreatedAt().compareTo(b.getCreatedAt());
                        case INGREDIENT_NAME -> a.getIngredientName().compareTo(b.getIngredientName());
                        case CATEGORY -> a.getCategory().getName().compareTo(b.getCategory().getName());
                        case LOCATION_EXPIRATION -> {
                            int locationComp = a.getLocation().compareTo(b.getLocation());
                            yield locationComp != 0 ? locationComp : compareExpirationDate(a, b);
                        }
                    };

                    return direction == RefrigeratorItemDto.SortDirection.DESC ? -comparison : comparison;
                })
                .collect(Collectors.toList());
    }

    /**
     * 유통기한 비교 (null 처리 포함)
     */
    private int compareExpirationDate(RefrigeratorItemDto.Response a, RefrigeratorItemDto.Response b) {
        if (a.getExpirationDate() == null && b.getExpirationDate() == null) return 0;
        if (a.getExpirationDate() == null) return 1;  // null은 뒤로
        if (b.getExpirationDate() == null) return -1;
        return a.getExpirationDate().compareTo(b.getExpirationDate());
    }
}