package org.example.be17pickcook.domain.refrigerator.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItemDto;
import org.example.be17pickcook.domain.refrigerator.service.RefrigeratorItemService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 냉장고 아이템 컨트롤러
 * - 식재료 CRUD API
 * - 검색, 필터링, 통계 API
 * - 소프트 삭제 및 복원 API
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/refrigerator/items")
public class RefrigeratorItemController {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final RefrigeratorItemService refrigeratorItemService;

    // =================================================================
    // 기본 CRUD 관련 API
    // =================================================================

    /**
     * 냉장고 아이템 추가
     */
    @PostMapping
    public ResponseEntity<BaseResponse<RefrigeratorItemDto.Response>> create(
            @Valid @RequestBody RefrigeratorItemDto.Request dto,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        Integer userId = getUserIdFromAuth(authentication);
        RefrigeratorItemDto.Response result = refrigeratorItemService.create(dto, userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 사용자별 냉장고 아이템 전체 조회
     */
    @GetMapping
    public ResponseEntity<BaseResponse<List<RefrigeratorItemDto.Response>>> findAll(
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        List<RefrigeratorItemDto.Response> result = refrigeratorItemService.findByUserId(userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 특정 냉장고 아이템 조회
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<BaseResponse<RefrigeratorItemDto.Response>> findById(
            @PathVariable Long itemId,
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        RefrigeratorItemDto.Response result = refrigeratorItemService.findById(itemId, userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 냉장고 아이템 수정
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<BaseResponse<RefrigeratorItemDto.Response>> update(
            @PathVariable Long itemId,
            @Valid @RequestBody RefrigeratorItemDto.Update dto,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        Integer userId = getUserIdFromAuth(authentication);
        RefrigeratorItemDto.Response result = refrigeratorItemService.update(itemId, dto, userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 냉장고 아이템 삭제 (소프트 삭제)
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<BaseResponse<Void>> delete(
            @PathVariable Long itemId,
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        refrigeratorItemService.delete(itemId, userId);

        return ResponseEntity.ok(BaseResponse.success(null));
    }

    /**
     * 삭제된 아이템 복원 (실행 취소)
     */
    @PostMapping("/{itemId}/undo")
    public ResponseEntity<BaseResponse<Void>> undoDelete(
            @PathVariable Long itemId,
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        refrigeratorItemService.undoDelete(itemId, userId);

        return ResponseEntity.ok(BaseResponse.success(null));
    }

    // =================================================================
    // 검색 관련 API
    // =================================================================

    /**
     * 재료명 키워드 검색
     */
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<List<RefrigeratorItemDto.Response>>> search(
            @RequestParam String keyword,
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        List<RefrigeratorItemDto.Response> result = refrigeratorItemService.searchByKeyword(keyword, userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 복합 필터링 조회
     */
    @PostMapping("/filter")
    public ResponseEntity<BaseResponse<List<RefrigeratorItemDto.Response>>> filter(
            @Valid @RequestBody RefrigeratorItemDto.Filter filter,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        Integer userId = getUserIdFromAuth(authentication);
        List<RefrigeratorItemDto.Response> result = refrigeratorItemService.findByFilters(filter, userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // =================================================================
    // 필터링 관련 API
    // =================================================================

    /**
     * 특정 위치의 아이템 조회
     */
    @GetMapping("/location/{location}")
    public ResponseEntity<BaseResponse<List<RefrigeratorItemDto.Response>>> findByLocation(
            @PathVariable String location,
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        List<RefrigeratorItemDto.Response> result = refrigeratorItemService.findByLocation(location, userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 특정 카테고리의 아이템 조회
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<BaseResponse<List<RefrigeratorItemDto.Response>>> findByCategory(
            @PathVariable Long categoryId,
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        List<RefrigeratorItemDto.Response> result = refrigeratorItemService.findByCategory(categoryId, userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // =================================================================
    // 기타 비즈니스 로직 API
    // =================================================================

    /**
     * 일괄 등록 (구매 → 냉장고 등록)
     */
    @PostMapping("/bulk")
    public ResponseEntity<BaseResponse<List<RefrigeratorItemDto.Response>>> createBulk(
            @Valid @RequestBody RefrigeratorItemDto.BulkRequest dto,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        Integer userId = getUserIdFromAuth(authentication);
        List<RefrigeratorItemDto.Response> result = refrigeratorItemService.createBulk(dto, userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 유통기한 임박 아이템 조회
     */
    @GetMapping("/expiring")
    public ResponseEntity<BaseResponse<List<RefrigeratorItemDto.Response>>> findExpiring(
            @RequestParam(defaultValue = "3") int days,
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        List<RefrigeratorItemDto.Response> result = refrigeratorItemService.findExpiringItems(userId, days);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 만료된 아이템 조회
     */
    @GetMapping("/expired")
    public ResponseEntity<BaseResponse<List<RefrigeratorItemDto.Response>>> findExpired(
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        List<RefrigeratorItemDto.Response> result = refrigeratorItemService.findExpiredItems(userId);

        return ResponseEntity.ok(BaseResponse.success(result));
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