package org.example.be17pickcook.domain.scrap.controller;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.domain.scrap.model.ScrapTargetType;
import org.example.be17pickcook.domain.scrap.service.ScrapService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scrap")
public class ScrapController {
    private final ScrapService scrapService;

    @PostMapping
    public BaseResponse scrap(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @RequestParam ScrapTargetType targetType,
            @RequestParam Long targetId) {
        scrapService.toggleScrap(authUser, targetType, targetId);


        return BaseResponse.success("스크랩 기능 성공");
    }
}
