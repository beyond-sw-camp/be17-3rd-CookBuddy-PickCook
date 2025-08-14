package org.example.be17pickcook.domain.community.controller;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.domain.community.model.CommentDto;
import org.example.be17pickcook.domain.community.service.CommentService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;

    // 게시글 댓글 목록 조회
    @GetMapping
    public BaseResponse<List<CommentDto.Response>> getComments(@RequestParam Long postId) {
        List<CommentDto.Response> comments = commentService.getCommentsByPost(postId);
        return BaseResponse.success(comments);
    }

    // 댓글 작성
    @PostMapping
    public BaseResponse<CommentDto.Response> createComment(@RequestBody CommentDto.Request commentDto, @AuthenticationPrincipal UserDto.AuthUser authUser) {
        CommentDto.Response saved = commentService.createComment(commentDto, authUser);
        return BaseResponse.success(saved);
    }

}
