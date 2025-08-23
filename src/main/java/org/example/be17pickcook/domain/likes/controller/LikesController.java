package org.example.be17pickcook.domain.likes.controller;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.domain.likes.model.LikeDto;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.likes.service.LikeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/like")
public class LikesController {
    private final LikeService likeService;

    /*
     * 좋아요 토글
     * @param authUser 로그인한 사용자 정보
     * @param type 대상 타입 (RECIPE, POST, COMMENT)
     * @param targetId 대상 ID
     */

    @PostMapping
    public BaseResponse<LikeDto.Response> like(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @RequestBody LikeDto.Request request) {

        likeService.toggleLike(authUser, request.getTargetType(), request.getTargetId());
        LikeDto.Response response = LikeDto.Response.builder()
                .likeCount(likeService.getLikeCount(request.getTargetType(), request.getTargetId()))
                .hasLiked(likeService.hasUserLiked(authUser.getIdx(), request.getTargetType(), request.getTargetId()))
                .build();

        return BaseResponse.success(response);
    }
}
