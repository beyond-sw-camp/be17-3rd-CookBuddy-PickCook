package org.example.be17pickcook.domain.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

/**
 * 리뷰 관리 컨트롤러
 * - 리뷰 CRUD API
 * - 상품별 리뷰 목록 조회
 * - 리뷰 필터링 및 정렬
 * - 사용자별 리뷰 관리
 */
@Tag(name = "리뷰 관리", description = "리뷰 작성, 조회, 수정, 삭제 기능을 제공합니다.")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final ReviewService reviewService;

    // =================================================================
    // 리뷰 작성 API
    // =================================================================

    @Operation(
            summary = "리뷰 작성",
            description = "구매한 상품에 대한 리뷰를 작성합니다. 별점, 텍스트 리뷰, 이미지 첨부가 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "리뷰 작성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "403", description = "리뷰 작성 권한 없음 (미구매 상품)")
            }
    )
    @PostMapping
    public ResponseEntity<BaseResponse<ReviewDto.Response>> createReview(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "리뷰 작성 정보", required = true)
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
            description = "본인이 작성한 리뷰를 수정합니다. (작성 후 7일 이내만 가능)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "403", description = "수정 권한 없음 (타인의 리뷰 또는 수정 기간 초과)"),
                    @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
            }
    )
    @PutMapping("/{reviewId}")
    public ResponseEntity<BaseResponse<ReviewDto.Response>> updateReview(
            @Parameter(description = "수정할 리뷰 ID", example = "1")
            @PathVariable Long reviewId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "리뷰 수정 정보", required = true)
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
            description = "본인이 작성한 리뷰를 삭제합니다. (소프트 삭제)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "리뷰 삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (타인의 리뷰)"),
                    @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
            }
    )
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<BaseResponse<Void>> deleteReview(
            @Parameter(description = "삭제할 리뷰 ID", example = "1")
            @PathVariable Long reviewId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        reviewService.deleteReview(reviewId, authUser.getIdx());
        return ResponseEntity.ok(BaseResponse.success(null));
    }

    // =================================================================
    // 상품별 리뷰 목록 조회 API
    // =================================================================

    @Operation(
            summary = "상품별 리뷰 목록 조회",
            description = "특정 상품의 리뷰를 필터링하여 조회합니다. " +
                    "별점, 기간, 이미지 유무, 정렬 방식 등 다양한 필터 옵션을 지원합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "리뷰 조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 필터 파라미터"),
                    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
            }
    )
    @GetMapping("/products/{productId}")
    public ResponseEntity<BaseResponse<ReviewDto.ListResponse>> getProductReviews(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long productId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "별점 필터 (1~5)", example = "5")
            @RequestParam(required = false) Integer rating,
            @Parameter(description = "기간 필터 (ALL, ONE_MONTH, THREE_MONTHS, SIX_MONTHS)", example = "ALL")
            @RequestParam(required = false) String period,
            @Parameter(description = "이미지 필터 (ALL, WITH_IMAGE, WITHOUT_IMAGE)", example = "ALL")
            @RequestParam(required = false) String imageFilter,
            @Parameter(description = "정렬 방식 (LATEST, OLDEST, RATING_HIGH, RATING_LOW)", example = "LATEST")
            @RequestParam(required = false) String sortType,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지당 리뷰 수", example = "20")
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
            description = "현재 사용자가 작성한 모든 리뷰를 조회합니다. 최신순으로 정렬되어 반환됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "리뷰 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/my")
    public ResponseEntity<BaseResponse<ReviewDto.ListResponse>> getMyReviews(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지당 리뷰 수", example = "20")
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