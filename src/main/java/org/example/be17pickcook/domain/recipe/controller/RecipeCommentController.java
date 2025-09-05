package org.example.be17pickcook.domain.recipe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.domain.recipe.model.RecipeCommentDto;
import org.example.be17pickcook.domain.recipe.service.RecipeCommentService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 레시피 댓글 컨트롤러
 * - 레시피 댓글 작성 및 조회 API
 * - 이미지 첨부 기능 지원
 * - 사용자 인증 기반 댓글 관리
 */
@Tag(name = "레시피 댓글 관리", description = "레시피 댓글 등록, 댓글 목록 조회 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipe/comment")
public class RecipeCommentController {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final RecipeCommentService commentService;

    // =================================================================
    // 레시피 댓글 관련 API
    // =================================================================

    @Operation(
            summary = "레시피 댓글 작성",
            description = "특정 레시피에 댓글을 작성합니다. 텍스트 댓글과 함께 이미지를 첨부할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글 작성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "레시피를 찾을 수 없음")
            }
    )
    @PostMapping
    public BaseResponse<RecipeCommentDto.Response> addComment(
            @Parameter(description = "댓글 작성 정보", required = true)
            @RequestPart("data") RecipeCommentDto.Request request,
            @Parameter(description = "첨부할 이미지 파일 (선택사항)")
            @RequestPart(value = "image", required = false) MultipartFile image,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser) throws SQLException, IOException {

        request.setRecipeId(request.getRecipeId());
        return BaseResponse.success(commentService.addComment(request, image, authUser.getIdx()));
    }

    @Operation(
            summary = "레시피 댓글 목록 조회",
            description = "특정 레시피의 모든 댓글을 조회합니다. 댓글 작성자 정보와 첨부된 이미지도 함께 반환됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글 조회 성공"),
                    @ApiResponse(responseCode = "404", description = "레시피를 찾을 수 없음")
            }
    )
    @GetMapping
    public BaseResponse<List<RecipeCommentDto.Response>> getComments(
            @Parameter(description = "댓글을 조회할 레시피 ID", required = true, example = "1")
            @RequestParam Long recipeId) {

        return BaseResponse.success(commentService.getComments(recipeId));
    }
}