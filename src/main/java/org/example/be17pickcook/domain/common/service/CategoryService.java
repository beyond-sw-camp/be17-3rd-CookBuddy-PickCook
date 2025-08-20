package org.example.be17pickcook.domain.common.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.exception.BaseException;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.domain.common.mapper.CategoryMapper;
import org.example.be17pickcook.domain.common.model.Category;
import org.example.be17pickcook.domain.common.model.CategoryDto;
import org.example.be17pickcook.domain.common.repository.CategoryRepository;
import org.example.be17pickcook.domain.refrigerator.repository.RefrigeratorItemRepository;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 카테고리 서비스
 * - 카테고리 기본 CRUD
 * - 사용자별 카테고리 통계 조회
 * - 냉장고 아이템과 연동된 카테고리 정보 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final CategoryRepository categoryRepository;
    private final RefrigeratorItemRepository refrigeratorItemRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    // =================================================================
    // 기본 CRUD 관련 API
    // =================================================================

    /**
     * 전체 카테고리 조회 (ID 순서대로)
     */
    public List<CategoryDto.Response> findAll() {
        List<Category> categories = categoryRepository.findAllByOrderById();
        return categoryMapper.entityListToResponseList(categories);
    }

    /**
     * 특정 카테고리 조회
     */
    public CategoryDto.Response findById(Long categoryId) {
        Category category = findCategoryById(categoryId);
        return categoryMapper.entityToResponse(category);
    }

    /**
     * 사용자별 카테고리 통계 정보 조회
     * - 각 카테고리별 아이템 개수
     * - 유통기한 임박 아이템 개수 (3일 이내)
     */
    public List<CategoryDto.Summary> findCategorySummaryByUserId(Integer userId) {
        validateUserExists(userId);

        // 모든 카테고리 조회
        List<Category> allCategories = categoryRepository.findAllByOrderById();

        // 카테고리별 아이템 개수 조회
        List<Object[]> categoryCountData = refrigeratorItemRepository.countItemsByCategoryForUser(userId);
        Map<Long, Integer> categoryCountMap = categoryCountData.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],           // category_id
                        row -> ((Number) row[2]).intValue()  // count
                ));

        // 카테고리별 유통기한 임박 아이템 개수 조회 (3일 이내)
        LocalDate urgentDate = LocalDate.now().plusDays(3);
        List<Object[]> urgentCountData = refrigeratorItemRepository.countExpiringItemsByCategoryForUser(userId, urgentDate);
        Map<Long, Integer> urgentCountMap = urgentCountData.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],           // category_id
                        row -> ((Number) row[1]).intValue()  // count
                ));

        // Summary DTO 생성
        return allCategories.stream()
                .map(category -> {
                    CategoryDto.Summary summary = categoryMapper.entityToSummary(category);
                    return summary.toBuilder()
                            .itemCount(categoryCountMap.getOrDefault(category.getId(), 0))
                            .expiringItemCount(urgentCountMap.getOrDefault(category.getId(), 0))
                            .build();
                })
                .collect(Collectors.toList());
    }

    // =================================================================
    // 검색 관련 API
    // =================================================================

    /**
     * 사용자가 실제 사용 중인 카테고리만 조회
     * - 냉장고에 아이템이 있는 카테고리만 반환
     */
    public List<CategoryDto.Response> findActiveCategoriesByUserId(Integer userId) {
        validateUserExists(userId);

        List<Category> activeCategories = categoryRepository.findActiveCategoriesByUserId(userId);
        return categoryMapper.entityListToResponseList(activeCategories);
    }

    /**
     * 전체적으로 사용되고 있는 카테고리 조회
     * - 모든 사용자 기준으로 냉장고에 아이템이 있는 카테고리
     */
    public List<CategoryDto.Response> findActiveCategoriesGlobal() {
        List<Category> activeCategories = categoryRepository.findActiveCategoriesGlobal();
        return categoryMapper.entityListToResponseList(activeCategories);
    }

    // =================================================================
    // 기타 비즈니스 로직 API
    // =================================================================

    /**
     * 카테고리명으로 조회
     */
    public CategoryDto.Response findByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.CATEGORY_NOT_FOUND));

        return categoryMapper.entityToResponse(category);
    }

    /**
     * 카테고리명 중복 확인
     */
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    // =================================================================
    // 관리자용 CRUD (향후 확장용)
    // =================================================================

    /**
     * 새 카테고리 추가 (관리자용)
     */
    @Transactional
    public CategoryDto.Response create(CategoryDto.Request dto) {
        // 카테고리명 중복 확인
        if (categoryRepository.existsByName(dto.getName())) {
            throw BaseException.from(BaseResponseStatus.CATEGORY_NAME_DUPLICATE);
        }

        Category entity = categoryMapper.requestToEntity(dto);
        Category savedEntity = categoryRepository.save(entity);

        return categoryMapper.entityToResponse(savedEntity);
    }

    /**
     * 카테고리 수정 (관리자용)
     */
    @Transactional
    public CategoryDto.Response update(Long categoryId, CategoryDto.Update dto) {
        Category existingCategory = findCategoryById(categoryId);

        // 카테고리명 변경 시 중복 확인
        if (dto.getName() != null && !dto.getName().equals(existingCategory.getName())) {
            if (categoryRepository.existsByName(dto.getName())) {
                throw BaseException.from(BaseResponseStatus.CATEGORY_NAME_DUPLICATE);
            }
        }

        categoryMapper.updateEntityFromDto(existingCategory, dto);
        Category updatedCategory = categoryRepository.save(existingCategory);

        return categoryMapper.entityToResponse(updatedCategory);
    }

    /**
     * 카테고리 삭제 (관리자용)
     * - 사용 중인 카테고리는 삭제 불가
     */
    @Transactional
    public void delete(Long categoryId) {
        Category category = findCategoryById(categoryId);

        // 사용 중인 카테고리인지 확인
        List<Category> activeCategories = categoryRepository.findActiveCategoriesGlobal();
        boolean isInUse = activeCategories.stream()
                .anyMatch(activeCategory -> activeCategory.getId().equals(categoryId));

        if (isInUse) {
            throw BaseException.from(BaseResponseStatus.CATEGORY_IN_USE_CANNOT_DELETE);
        }

        categoryRepository.delete(category);
    }

    // =================================================================
    // 유틸리티 메서드들
    // =================================================================

    /**
     * 카테고리 ID로 Category 엔티티 조회
     */
    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.CATEGORY_NOT_FOUND));
    }

    /**
     * 사용자 존재 확인
     */
    private void validateUserExists(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw BaseException.from(BaseResponseStatus.USER_NOT_FOUND);
        }
    }
}