package org.example.be17pickcook.domain.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.domain.community.model.CommentDto;
import org.example.be17pickcook.domain.community.service.CommentService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 커뮤니티 댓글 컨트롤러
 * - 게시글 댓글 조회, 작성 API
 * - 사용자 인증 기반 댓글 관리
 * - 좋아요 기능과 연동된 댓글 시스템
 */
@Tag(name = "커뮤니티 댓글 관리", description = "게시글 댓글 조회 및 작성 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final CommentService commentService;

    // =================================================================
    // 댓글 관련 API
    // =================================================================

    @Operation(
            summary = "게시글 댓글 목록 조회",
            description = "특정 게시글의 댓글 목록을 조회합니다. 로그인한 사용자의 좋아요 상태도 함께 반환됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "댓글 조회 성공",
                            content = @Content(schema = @Schema(implementation = CommentDto.Response.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
            }
    )
    @GetMapping
    public BaseResponse<List<CommentDto.Response>> getComments(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "댓글을 조회할 게시글 ID", required = true, example = "1")
            @RequestParam Long postId) {

        List<CommentDto.Response> comments = commentService.getCommentsByPost(authUser.getIdx(), postId);
        return BaseResponse.success(comments);
    }

    @Operation(
            summary = "댓글 작성",
            description = "게시글에 댓글을 작성합니다. 로그인한 사용자만 댓글을 작성할 수 있습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "댓글 작성 성공",
                            content = @Content(schema = @Schema(implementation = CommentDto.Response.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
            }
    )
    @PostMapping
    public BaseResponse<CommentDto.Response> createComment(
            @Parameter(description = "작성할 댓글 정보", required = true)
            @RequestBody CommentDto.Request commentDto,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        CommentDto.Response saved = commentService.createComment(commentDto, authUser.getIdx());
        return BaseResponse.success(saved);
    }
}