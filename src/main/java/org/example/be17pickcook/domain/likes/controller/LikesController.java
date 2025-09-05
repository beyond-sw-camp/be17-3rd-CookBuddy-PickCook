package org.example.be17pickcook.domain.likes.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.likes.service.LikeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 좋아요 기능 컨트롤러
 * - 레시피, 게시글, 댓글에 대한 좋아요 토글 기능
 * - 사용자별 좋아요 상태 관리
 * - 좋아요 수 실시간 업데이트
 */
@Tag(name = "좋아요 관리", description = "레시피, 게시글, 댓글에 대한 좋아요 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/like")
public class LikesController {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final LikeService likeService;

    // =================================================================
    // 좋아요 관련 API
    // =================================================================

    /**
     * 좋아요 토글
     * @param authUser 로그인한 사용자 정보
     * @param targetType 대상 타입 (RECIPE, POST, COMMENT)
     * @param targetId 대상 ID
     */
    @Operation(
            summary = "좋아요 토글",
            description = "레시피, 게시글, 댓글에 좋아요를 추가하거나 취소합니다. " +
                    "이미 좋아요를 눌렀으면 취소되고, 누르지 않았으면 추가됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좋아요 처리 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (존재하지 않는 대상)"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음")
            }
    )
    @PostMapping
    public BaseResponse<String> like(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(
                    description = "좋아요 대상 타입",
                    required = true,
                    example = "RECIPE"
            )
            @RequestParam LikeTargetType targetType,
            @Parameter(
                    description = "좋아요 대상 ID",
                    required = true,
                    example = "1"
            )
            @RequestParam Long targetId) {

        likeService.toggleLike(authUser, targetType, targetId);
        return BaseResponse.success("좋아요 기능 성공");
    }
}