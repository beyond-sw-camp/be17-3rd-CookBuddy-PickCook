package org.example.be17pickcook.domain.common.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.domain.common.model.CategoryDto;
import org.example.be17pickcook.domain.common.service.CategoryService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 컨트롤러
 * - 카테고리 기본 조회 API
 * - 사용자별 카테고리 통계 API
 * - 관리자용 CRUD API (향후 확장)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final CategoryService categoryService;

    // =================================================================
    // 기본 CRUD 관련 API
    // =================================================================

    /**
     * 전체 카테고리 조회
     */
    @GetMapping
    public ResponseEntity<BaseResponse<List<CategoryDto.Response>>> findAll() {
        List<CategoryDto.Response> result = categoryService.findAll();
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 특정 카테고리 조회
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<BaseResponse<CategoryDto.Response>> findById(
            @PathVariable Long categoryId) {

        CategoryDto.Response result = categoryService.findById(categoryId);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 사용자별 카테고리 통계 정보 조회
     * - 각 카테고리별 아이템 개수
     * - 유통기한 임박 아이템 개수
     */
    @GetMapping("/summary")
    public ResponseEntity<BaseResponse<List<CategoryDto.Summary>>> findSummary(
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        List<CategoryDto.Summary> result = categoryService.findCategorySummaryByUserId(userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // =================================================================
    // 검색 관련 API
    // =================================================================

    /**
     * 사용자가 실제 사용 중인 카테고리 조회
     */
    @GetMapping("/active")
    public ResponseEntity<BaseResponse<List<CategoryDto.Response>>> findActiveByUser(
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        List<CategoryDto.Response> result = categoryService.findActiveCategoriesByUserId(userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 전체적으로 사용되고 있는 카테고리 조회
     */
    @GetMapping("/active/global")
    public ResponseEntity<BaseResponse<List<CategoryDto.Response>>> findActiveGlobal() {
        List<CategoryDto.Response> result = categoryService.findActiveCategoriesGlobal();
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 카테고리명으로 조회
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<BaseResponse<CategoryDto.Response>> findByName(
            @PathVariable String name) {

        CategoryDto.Response result = categoryService.findByName(name);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 카테고리명 중복 확인
     */
    @GetMapping("/check-name")
    public ResponseEntity<BaseResponse<Boolean>> checkNameExists(
            @RequestParam String name) {

        boolean exists = categoryService.existsByName(name);
        return ResponseEntity.ok(BaseResponse.success(exists));
    }

    // =================================================================
    // 관리자용 CRUD API (향후 확장용)
    // =================================================================

    /**
     * 새 카테고리 추가 (관리자용)
     */
    @PostMapping
    public ResponseEntity<BaseResponse<CategoryDto.Response>> create(
            @Valid @RequestBody CategoryDto.Request dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        CategoryDto.Response result = categoryService.create(dto);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 카테고리 수정 (관리자용)
     */
    @PutMapping("/{categoryId}")
    public ResponseEntity<BaseResponse<CategoryDto.Response>> update(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryDto.Update dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        CategoryDto.Response result = categoryService.update(categoryId, dto);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 카테고리 삭제 (관리자용)
     */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<BaseResponse<Void>> delete(
            @PathVariable Long categoryId) {

        categoryService.delete(categoryId);
        return ResponseEntity.ok(BaseResponse.success(null));
    }

    // =================================================================
    // 유틸리티 메서드들
    // =================================================================

    /**
     * Authentication에서 사용자 ID 추출
     */
    private Integer getUserIdFromAuth(Authentication authentication) {
        UserDto.AuthUser authUser = (UserDto.AuthUser) authentication.getPrincipal();
        return authUser.getIdx();
    }
}