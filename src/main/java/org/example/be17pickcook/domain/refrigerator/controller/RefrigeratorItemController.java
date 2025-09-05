package org.example.be17pickcook.domain.refrigerator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 냉장고 아이템 컨트롤러 (필터링 시스템 중심으로 정리됨)
 * - 식재료 CRUD API
 * - 검색, 필터링 API
 * - 소프트 삭제 및 복원 API
 */
@Tag(name = "냉장고 관리", description = "냉장고 식재료 등록, 조회, 수정, 삭제 및 검색/필터링 기능을 제공합니다.")
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

    @Operation(
            summary = "냉장고 식재료 추가",
            description = "냉장고에 새로운 식재료를 추가합니다. 재료명, 카테고리, 수량, 유통기한, 보관위치를 등록할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "식재료 등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @PostMapping
    public ResponseEntity<BaseResponse<RefrigeratorItemDto.Response>> create(
            @Parameter(description = "등록할 식재료 정보")
            @Valid @RequestBody RefrigeratorItemDto.Request dto,
            BindingResult bindingResult,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
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

    @Operation(
            summary = "전체 냉장고 식재료 조회",
            description = "사용자의 모든 냉장고 식재료를 위치별로 정렬하여 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping
    public ResponseEntity<BaseResponse<List<RefrigeratorItemDto.Response>>> findAll(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        List<RefrigeratorItemDto.Response> result = refrigeratorItemService.findByUserId(userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @Operation(
            summary = "특정 냉장고 식재료 조회",
            description = "식재료 ID로 특정 냉장고 식재료의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "식재료를 찾을 수 없음"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/{itemId}")
    public ResponseEntity<BaseResponse<RefrigeratorItemDto.Response>> findById(
            @Parameter(description = "조회할 식재료 ID", example = "1")
            @PathVariable Long itemId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        RefrigeratorItemDto.Response result = refrigeratorItemService.findById(itemId, userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @Operation(
            summary = "냉장고 식재료 수정",
            description = "기존 냉장고 식재료의 정보를 수정합니다. 재료명, 수량, 유통기한, 카테고리, 보관위치를 변경할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "404", description = "식재료를 찾을 수 없음"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @PutMapping("/{itemId}")
    public ResponseEntity<BaseResponse<RefrigeratorItemDto.Response>> update(
            @Parameter(description = "수정할 식재료 ID", example = "1")
            @PathVariable Long itemId,
            @Parameter(description = "수정할 식재료 정보")
            @Valid @RequestBody RefrigeratorItemDto.Update dto,
            BindingResult bindingResult,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
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

    @Operation(
            summary = "냉장고 식재료 삭제",
            description = "냉장고 식재료를 소프트 삭제합니다. 실제로는 삭제 표시만 하며, 실행 취소가 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "식재료를 찾을 수 없음"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @DeleteMapping("/{itemId}")
    public ResponseEntity<BaseResponse<Void>> delete(
            @Parameter(description = "삭제할 식재료 ID", example = "1")
            @PathVariable Long itemId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        refrigeratorItemService.delete(itemId, userId);

        return ResponseEntity.ok(BaseResponse.success(null));
    }

    @Operation(
            summary = "삭제된 식재료 복원 (실행 취소)",
            description = "소프트 삭제된 식재료를 복원합니다. 삭제 후 5초 내에 실행 취소할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "복원 성공"),
                    @ApiResponse(responseCode = "404", description = "복원할 식재료를 찾을 수 없음"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @PostMapping("/{itemId}/undo")
    public ResponseEntity<BaseResponse<Void>> undoDelete(
            @Parameter(description = "복원할 식재료 ID", example = "1")
            @PathVariable Long itemId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        refrigeratorItemService.undoDelete(itemId, userId);

        return ResponseEntity.ok(BaseResponse.success(null));
    }

    // =================================================================
    // 검색 관련 API
    // =================================================================

    @Operation(
            summary = "복합 필터링 조회",
            description = "키워드, 카테고리, 유통기한 상태, 정렬 옵션을 조합하여 냉장고 아이템을 필터링합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "필터링 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 필터 조건"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @PostMapping("/filter")
    public ResponseEntity<BaseResponse<List<RefrigeratorItemDto.Response>>> filter(
            @Parameter(description = "필터링 조건")
            @Valid @RequestBody RefrigeratorItemDto.Filter filter,
            BindingResult bindingResult,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
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

    @Operation(
            summary = "카테고리별 식재료 조회",
            description = "특정 카테고리에 속한 냉장고 식재료를 모두 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<BaseResponse<List<RefrigeratorItemDto.Response>>> findByCategory(
            @Parameter(description = "카테고리 ID", example = "1")
            @PathVariable Long categoryId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        List<RefrigeratorItemDto.Response> result = refrigeratorItemService.findByCategory(categoryId, userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // =================================================================
    // 기타 비즈니스 로직 API
    // =================================================================

    @Operation(
            summary = "식재료 일괄 등록",
            description = "쇼핑몰에서 구매한 상품들을 냉장고에 일괄로 등록합니다. 구매 완료 후 냉장고 등록 시 사용됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "일괄 등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @PostMapping("/bulk")
    public ResponseEntity<BaseResponse<List<RefrigeratorItemDto.Response>>> createBulk(
            @Parameter(description = "일괄 등록할 식재료 목록")
            @Valid @RequestBody RefrigeratorItemDto.BulkRequest dto,
            BindingResult bindingResult,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
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

    @Operation(
            summary = "유통기한 임박 식재료 조회",
            description = "유통기한이 임박한 식재료를 조회합니다. 기본값은 3일 이내이며, 일수를 조정할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/expiring")
    public ResponseEntity<BaseResponse<List<RefrigeratorItemDto.Response>>> findExpiring(
            @Parameter(description = "기준 일수", example = "3")
            @RequestParam(defaultValue = "3") int days,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        List<RefrigeratorItemDto.Response> result = refrigeratorItemService.findExpiringItems(userId, days);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @Operation(
            summary = "유통기한 만료된 식재료 조회",
            description = "유통기한이 이미 지난 식재료를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/expired")
    public ResponseEntity<BaseResponse<List<RefrigeratorItemDto.Response>>> findExpired(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        List<RefrigeratorItemDto.Response> result = refrigeratorItemService.findExpiredItems(userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @Operation(
            summary = "냉장고 동기화 안내 메시지 조회",
            description = "냉장고 페이지 접속 시 표시할 동기화 안내 메시지를 조회합니다. 24시간 경과 또는 새 구매가 있을 때 안내합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/sync-prompt")
    public ResponseEntity<BaseResponse<RefrigeratorItemDto.SyncPrompt>> getSyncPrompt(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        RefrigeratorItemDto.SyncPrompt result = refrigeratorItemService.getSyncPrompt(userId);

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