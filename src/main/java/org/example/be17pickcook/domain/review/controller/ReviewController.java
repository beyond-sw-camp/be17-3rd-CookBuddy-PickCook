package org.example.be17pickcook.domain.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.review.model.ReviewDto;
import org.example.be17pickcook.domain.review.service.ReviewService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.http.MediaType;
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
            description = """
                    사용자가 새로운 리뷰를 등록합니다.
                    - 리뷰 본문은 @RequestPart("review")로 JSON 전달
                    - 이미지 파일은 선택(List<MultipartFile>)로 @RequestPart("files") 전송 가능
                    """
    )
    @PostMapping(
            value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> register(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Valid @RequestPart("review") ReviewDto.ReviewRequestDto dto,
            @RequestPart(name = "files", required = false) List<MultipartFile> files
    ) {
        // 인증 널가드
        if (authUser == null || authUser.getIdx() == null) {
            return ResponseEntity.status(401).body("인증 필요 혹은 사용자 ID 누락");
        }

        // Integer든 Long이든 모두 안전하게 Long으로 변환
        Long userId = (authUser.getIdx() == null)
                ? null
                : Long.valueOf(authUser.getIdx().longValue());

        reviewService.register(userId, dto, files);
        return ResponseEntity.ok("리뷰 작성 성공");
    }

    @Operation(
            summary = "특정 상품 리뷰 전체 조회",
            description = "상품 ID를 기반으로 리뷰 정보를 전체 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<List<ReviewDto.ReviewResponseDto>> getReviewByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getAllReviewByProductId(productId));
    }
}
