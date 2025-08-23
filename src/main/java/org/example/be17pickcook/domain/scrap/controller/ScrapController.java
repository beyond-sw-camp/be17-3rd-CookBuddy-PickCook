package org.example.be17pickcook.domain.scrap.controller;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.domain.scrap.model.ScrapDto;
import org.example.be17pickcook.domain.scrap.service.ScrapService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scrap")
public class ScrapController {
    private final ScrapService scrapService;

    @PostMapping
    public BaseResponse<ScrapDto.Response> scrap(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @RequestBody ScrapDto.Request request) {
        scrapService.toggleScrap(authUser, request.getTargetType(), request.getTargetId());
        ScrapDto.Response response = ScrapDto.Response.builder()
                .scrapCount(scrapService.getScrapCount(request.getTargetType(), request.getTargetId()))
                .hasScrapped(scrapService.hasUserScrapped(authUser.getIdx(), request.getTargetType(), request.getTargetId()))
                .build();

        return BaseResponse.success(response);
    }
}
