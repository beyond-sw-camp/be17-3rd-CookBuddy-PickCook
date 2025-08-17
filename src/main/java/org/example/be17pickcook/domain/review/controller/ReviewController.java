package org.example.be17pickcook.domain.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.review.model.ReviewDto;
import org.example.be17pickcook.domain.review.service.ReviewService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@Tag(name = "리뷰 기능", description = "리뷰 등록, 조회, 목록 조회 기능을 제공합니다.")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @Operation(
            summary = "리뷰 등록",
            description = "사용자가 새로운 리뷰를 등록합니다.\n" +
                    "- 리뷰 정보는 RecipeRequestDto로 전달\n" +
                    "- 이미지 파일은 optional로 MultipartFile 리스트 형태로 전달 가능"
    )
    @PostMapping("/register")
    public ResponseEntity register(@AuthenticationPrincipal UserDto.AuthUser authUser,
                                   @ModelAttribute ReviewDto.ReviewRequestDto dto,
                                   @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        reviewService.register(authUser, dto, files);

        return ResponseEntity.status(200).body("리뷰 작성 성공");
    }
}
