package org.example.be17pickcook.domain.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.domain.community.model.CommentDto;
import org.example.be17pickcook.domain.community.service.CommentService;
import org.example.be17pickcook.domain.likes.service.LikeService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;

    @Operation(
            summary = "게시글 댓글 목록 조회",
            description = "특정 게시글의 댓글 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "댓글 조회 성공",
                            content = @Content(schema = @Schema(implementation = CommentDto.Response.class))
                    )
            }
    )
    @GetMapping
    public BaseResponse<List<CommentDto.Response>> getComments(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "댓글을 조회할 게시글 ID", required = true)
            @RequestParam Long postId) {
        List<CommentDto.Response> comments = commentService.getCommentsByPost(authUser.getIdx(), postId);
        return BaseResponse.success(comments);
    }

    @Operation(
            summary = "댓글 작성",
            description = "게시글에 댓글을 작성합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "댓글 작성 성공",
                            content = @Content(schema = @Schema(implementation = CommentDto.Response.class))
                    )
            }
    )
    @PostMapping
    public BaseResponse<CommentDto.Response> createComment(
            @Parameter(description = "작성할 댓글 정보", required = true)
            @RequestBody CommentDto.Request commentDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser) {
        CommentDto.Response saved = commentService.createComment(commentDto, authUser.getIdx());
        return BaseResponse.success(saved);
    }

}
