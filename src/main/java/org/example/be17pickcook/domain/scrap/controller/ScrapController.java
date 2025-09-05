package org.example.be17pickcook.domain.scrap.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.domain.scrap.model.ScrapTargetType;
import org.example.be17pickcook.domain.scrap.service.ScrapService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 스크랩 기능 컨트롤러
 * - 레시피, 게시글에 대한 스크랩 토글 기능
 * - 사용자별 스크랩 상태 관리
 * - 스크랩 수 실시간 업데이트
 */
@Tag(name = "스크랩 관리", description = "레시피, 게시글에 대한 스크랩 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scrap")
public class ScrapController {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final ScrapService scrapService;

    // =================================================================
    // 스크랩 관련 API
    // =================================================================

    @Operation(
            summary = "스크랩 토글",
            description = "레시피나 게시글에 스크랩을 추가하거나 취소합니다. " +
                    "이미 스크랩했으면 취소되고, 하지 않았으면 추가됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "스크랩 처리 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (존재하지 않는 대상)"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없음")
            }
    )
    @PostMapping
    public BaseResponse<String> scrap(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(
                    description = "스크랩 대상 타입",
                    required = true,
                    example = "RECIPE"
            )
            @RequestParam ScrapTargetType targetType,
            @Parameter(
                    description = "스크랩 대상 ID",
                    required = true,
                    example = "1"
            )
            @RequestParam Long targetId) {

        scrapService.toggleScrap(authUser, targetType, targetId);
        return BaseResponse.success("스크랩 기능 성공");
    }
}