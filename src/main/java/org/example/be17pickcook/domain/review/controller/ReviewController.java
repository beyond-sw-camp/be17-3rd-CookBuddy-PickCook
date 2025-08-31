package org.example.be17pickcook.domain.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.domain.review.model.ReviewDto;
import org.example.be17pickcook.domain.review.service.ReviewService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리뷰 관리", description = "리뷰 작성, 조회, 수정, 삭제 기능을 제공합니다.")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // =================================================================
    // 리뷰 작성 API
    // =================================================================

    @Operation(
            summary = "리뷰 작성",
            description = "구매한 상품에 대한 리뷰를 작성합니다."
    )
    @PostMapping
    public ResponseEntity<BaseResponse<ReviewDto.Response>> createReview(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Valid @RequestBody ReviewDto.WriteRequest dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        ReviewDto.Response result = reviewService.createReview(authUser.getIdx(), dto);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // =================================================================
    // 리뷰 수정 API
    // =================================================================

    @Operation(
            summary = "리뷰 수정",
            description = "본인이 작성한 리뷰를 수정합니다. (작성 후 7일 이내만 가능)"
    )
    @PutMapping("/{reviewId}")
    public ResponseEntity<BaseResponse<ReviewDto.Response>> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Valid @RequestBody ReviewDto.UpdateRequest dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        ReviewDto.Response result = reviewService.updateReview(reviewId, authUser.getIdx(), dto);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // =================================================================
    // 리뷰 삭제 API
    // =================================================================

    @Operation(
            summary = "리뷰 삭제",
            description = "본인이 작성한 리뷰를 삭제합니다. (소프트 삭제)"
    )
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<BaseResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        reviewService.deleteReview(reviewId, authUser.getIdx());
        return ResponseEntity.ok(BaseResponse.success(null));
    }

    // =================================================================
    // 상품별 리뷰 목록 조회 API
    // =================================================================

    @Operation(
            summary = "상품별 리뷰 목록 조회",
            description = "특정 상품의 리뷰를 필터링하여 조회합니다."
    )
    @GetMapping("/products/{productId}")
    public ResponseEntity<BaseResponse<ReviewDto.ListResponse>> getProductReviews(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            // @ModelAttribute 대신 개별 @RequestParam 사용
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String imageFilter,
            @RequestParam(required = false) String sortType,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        System.out.println("=== 개별 파라미터로 받은 값들 ===");
        System.out.println("rating: " + rating);
        System.out.println("period: " + period);
        System.out.println("imageFilter: " + imageFilter);
        System.out.println("sortType: " + sortType);

        // Enum 변환
        ReviewDto.PeriodFilter periodEnum = period != null ?
                ReviewDto.PeriodFilter.valueOf(period) : null;
        ReviewDto.ImageFilter imageFilterEnum = imageFilter != null ?
                ReviewDto.ImageFilter.valueOf(imageFilter) : null;
        ReviewDto.SortType sortTypeEnum = sortType != null ?
                ReviewDto.SortType.valueOf(sortType) : ReviewDto.SortType.LATEST;

        ReviewDto.FilterRequest filter = ReviewDto.FilterRequest.builder()
                .productId(productId)
                .rating(rating)
                .period(periodEnum)
                .imageFilter(imageFilterEnum)
                .sortType(sortTypeEnum)
                .page(page)
                .size(size)
                .build();

        Integer currentUserId = authUser != null ? authUser.getIdx() : null;
        ReviewDto.ListResponse result = reviewService.getProductReviews(filter, currentUserId);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // =================================================================
    // 내 리뷰 목록 조회 API
    // =================================================================

    @Operation(
            summary = "내 리뷰 목록 조회",
            description = "현재 사용자가 작성한 모든 리뷰를 조회합니다."
    )
    @GetMapping("/my")
    public ResponseEntity<BaseResponse<ReviewDto.ListResponse>> getMyReviews(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        ReviewDto.FilterRequest filter = ReviewDto.FilterRequest.builder()
                .page(page)
                .size(size)
                .sortType(ReviewDto.SortType.LATEST)
                .build();

        ReviewDto.ListResponse result = reviewService.getMyReviews(authUser.getIdx(), filter);
        return ResponseEntity.ok(BaseResponse.success(result));
    }
}