package org.example.be17pickcook.likes.controller;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.likes.model.LikeTargetType;
import org.example.be17pickcook.likes.service.LikesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/likes")
public class LikesController {
    private final LikesService likesService;

    /*
     * 좋아요 토글
     * @param authUser 로그인한 사용자 정보
     * @param type 대상 타입 (FEED, RECIPE, COMMUNITY)
     * @param targetId 대상 ID
     */

    // 요청 형식
    // 레시피 좋아요 : POST likes?targetType=RECIPE&targetId=123
    // 커뮤니티 좋아요 : POST likes?targetType=RECIPE&targetId=123

    @PostMapping
    public ResponseEntity likes(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @RequestParam LikeTargetType targetType,
            @RequestParam Long targetId) {
        likesService.toggleLike(authUser, targetType, targetId);

        return ResponseEntity.status(200).body("좋아요 기능 성공");
    }
}
