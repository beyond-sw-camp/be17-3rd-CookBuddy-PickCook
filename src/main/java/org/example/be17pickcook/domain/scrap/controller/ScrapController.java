package org.example.be17pickcook.domain.scrap.controller;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.scrap.model.ScrapTargetType;
import org.example.be17pickcook.domain.scrap.service.ScrapService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scrap")
public class ScrapController {
    private final ScrapService scrapService;

    @PostMapping
    public ResponseEntity scrap(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @RequestParam ScrapTargetType targetType,
            @RequestParam Long targetId) {
        scrapService.toggleScrap(authUser, targetType, targetId);

        return ResponseEntity.status(200).body("스크랩 기능 성공");
    }
}
