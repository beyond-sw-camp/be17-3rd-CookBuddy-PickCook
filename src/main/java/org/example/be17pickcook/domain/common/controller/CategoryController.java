package org.example.be17pickcook.domain.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "카테고리 관리", description = "식재료 카테고리 조회, 통계, 관리 기능을 제공합니다.")
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
    @Operation(
            summary = "전체 카테고리 목록 조회",
            description = "시스템에 등록된 모든 식재료 카테고리를 조회합니다. 냉장고 등록 시 카테고리 선택에 사용됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @GetMapping
    public ResponseEntity<BaseResponse<List<CategoryDto.Response>>> findAll() {
        List<CategoryDto.Response> result = categoryService.findAll();
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 특정 카테고리 조회
     */
    @Operation(
            summary = "특정 카테고리 상세 조회",
            description = "카테고리 ID로 특정 카테고리의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
            }
    )
    @GetMapping("/{categoryId}")
    public ResponseEntity<BaseResponse<CategoryDto.Response>> findById(
            @Parameter(description = "조회할 카테고리 ID", example = "1")
            @PathVariable Long categoryId) {

        CategoryDto.Response result = categoryService.findById(categoryId);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // =================================================================
    // 검색 관련 API
    // =================================================================

    /**
     * 사용자가 실제 사용 중인 카테고리 조회
     */
    @Operation(
            summary = "사용자가 사용 중인 카테고리 조회",
            description = "현재 사용자의 냉장고에 등록된 식재료가 있는 카테고리만 조회합니다. 필터링에 유용합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/active")
    public ResponseEntity<BaseResponse<List<CategoryDto.Response>>> findActiveByUser(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            Authentication authentication) {

        Integer userId = getUserIdFromAuth(authentication);
        List<CategoryDto.Response> result = categoryService.findActiveCategoriesByUserId(userId);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 전체적으로 사용되고 있는 카테고리 조회
     */
    @Operation(
            summary = "전체 사용자가 사용 중인 카테고리 조회",
            description = "모든 사용자들이 실제로 사용하고 있는 카테고리만 조회합니다. 인기 카테고리 분석에 활용됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @GetMapping("/active/global")
    public ResponseEntity<BaseResponse<List<CategoryDto.Response>>> findActiveGlobal() {
        List<CategoryDto.Response> result = categoryService.findActiveCategoriesGlobal();
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 카테고리명으로 조회
     */
    @Operation(
            summary = "카테고리명으로 검색",
            description = "카테고리명으로 특정 카테고리를 검색합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "검색 성공"),
                    @ApiResponse(responseCode = "404", description = "해당 이름의 카테고리를 찾을 수 없음")
            }
    )
    @GetMapping("/name/{name}")
    public ResponseEntity<BaseResponse<CategoryDto.Response>> findByName(
            @Parameter(description = "검색할 카테고리명", example = "채소")
            @PathVariable String name) {

        CategoryDto.Response result = categoryService.findByName(name);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 카테고리명 중복 확인
     */
    @Operation(
            summary = "카테고리명 중복 확인",
            description = "카테고리 등록 시 이름 중복을 확인합니다. 관리자 기능용입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "확인 완료")
            }
    )
    @GetMapping("/check-name")
    public ResponseEntity<BaseResponse<Boolean>> checkNameExists(
            @Parameter(description = "확인할 카테고리명", example = "새카테고리")
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
    @Operation(
            summary = "새 카테고리 추가 (관리자용)",
            description = "시스템에 새로운 식재료 카테고리를 추가합니다. 관리자 권한이 필요합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "카테고리 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "409", description = "이미 존재하는 카테고리명")
            }
    )
    @PostMapping
    public ResponseEntity<BaseResponse<CategoryDto.Response>> create(
            @Parameter(description = "생성할 카테고리 정보")
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
    @Operation(
            summary = "카테고리 정보 수정 (관리자용)",
            description = "기존 카테고리의 정보를 수정합니다. 관리자 권한이 필요합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
                    @ApiResponse(responseCode = "409", description = "이미 존재하는 카테고리명")
            }
    )
    @PutMapping("/{categoryId}")
    public ResponseEntity<BaseResponse<CategoryDto.Response>> update(
            @Parameter(description = "수정할 카테고리 ID", example = "1")
            @PathVariable Long categoryId,
            @Parameter(description = "수정할 카테고리 정보")
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
    @Operation(
            summary = "카테고리 삭제 (관리자용)",
            description = "카테고리를 삭제합니다. 해당 카테고리를 사용하는 식재료가 있으면 삭제가 제한될 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
                    @ApiResponse(responseCode = "409", description = "사용 중인 카테고리는 삭제할 수 없음")
            }
    )
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<BaseResponse<Void>> delete(
            @Parameter(description = "삭제할 카테고리 ID", example = "1")
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