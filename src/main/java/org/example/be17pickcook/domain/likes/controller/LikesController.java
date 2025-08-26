package org.example.be17pickcook.domain.likes.controller;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.domain.likes.model.LikeTargetType;
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
    public BaseResponse like(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @RequestParam LikeTargetType targetType,
            @RequestParam Long targetId) {

        likeService.toggleLike(authUser, targetType, targetId);

        return BaseResponse.success("좋아요 기능 성공");
    }
}
