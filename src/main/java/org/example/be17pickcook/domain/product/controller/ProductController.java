package org.example.be17pickcook.domain.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.PageResponse;
import org.example.be17pickcook.domain.product.model.ProductDto;
import org.example.be17pickcook.domain.product.service.ProductService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 상품 관리 컨트롤러
 * - 상품 CRUD API
 * - 상품 검색 및 필터링
 * - 리뷰 정보 포함 상품 조회
 * - 레시피 연관 상품 추천
 */
@Tag(name = "상품 관리", description = "상품 등록, 조회, 수정, 삭제 및 검색 기능을 제공합니다.")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final ProductService productService;

    // =================================================================
    // 상품 등록 관련 API
    // =================================================================

    @Operation(
            summary = "상품 등록",
            description = "새로운 상품을 등록합니다. 상품 정보와 함께 이미지 파일(선택사항)을 업로드할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "상품 등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<String>> register(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "등록할 상품 정보")
            @RequestPart ProductDto.Register dto,
            @Parameter(description = "상품 이미지 파일 (선택사항)")
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws SQLException, IOException {
        productService.register(authUser, dto, files);
        return ResponseEntity.ok(BaseResponse.success("상품 등록 성공!"));
    }

    // =================================================================
    // 상품 조회 관련 API
    // =================================================================

    @Operation(
            summary = "상품 목록 조회 (리뷰 포함)",
            description = "상품 목록을 페이징하여 조회하며, 각 상품의 리뷰 정보도 함께 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @GetMapping("list-with-reviews")
    public ResponseEntity<Page<ProductDto.Response>> getProducts(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 아이템 수", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준", example = "productId")
            @RequestParam(defaultValue = "productId") String sortBy
    ) {
        Page<ProductDto.Response> productPage = productService.getPagedProductsWithReviewsDto(page, size, sortBy);
        return ResponseEntity.ok(productPage);
    }

    @Operation(
            summary = "상품 목록 조회 (페이징)",
            description = "상품 목록을 페이징과 정렬 옵션과 함께 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @GetMapping
    public BaseResponse<PageResponse<ProductDto.ProductListResponse>> getProductList(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 필드", example = "id")
            @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "정렬 방향 (ASC/DESC)", example = "ASC")
            @RequestParam(defaultValue = "ASC") String dir
    ) {
        Integer userIdx = (authUser != null) ? authUser.getIdx() : null;
        Sort s = dir.equalsIgnoreCase("DESC")
                ? Sort.by(sort).descending()
                : Sort.by(sort).ascending();
        Pageable pageable = PageRequest.of(page, size, s);
        return BaseResponse.success(productService.getProductList(userIdx, pageable));
    }

    @Operation(
            summary = "상품 상세 조회",
            description = "상품 ID로 특정 상품의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
            }
    )
    @GetMapping("/{id}")
    public ProductDto.Res findById(
            @Parameter(description = "조회할 상품 ID", example = "1")
            @PathVariable Long id) {
        return productService.findById(id);
    }

    @Operation(
            summary = "상품 상세 조회 (리뷰 포함)",
            description = "상품 ID로 특정 상품의 상세 정보와 리뷰 정보를 함께 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
            }
    )
    @GetMapping("/{id}/with-reviews")
    public ResponseEntity<BaseResponse<ProductDto.DetailWithReview>> getProductDetailWithReview(
            @Parameter(description = "조회할 상품 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        Integer currentUserId = authUser != null ? authUser.getIdx() : null;
        ProductDto.DetailWithReview result = productService.getProductDetailWithReview(id, currentUserId);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // =================================================================
    // 상품 수정 관련 API
    // =================================================================

    @Operation(
            summary = "상품 정보 수정",
            description = "상품 ID로 특정 상품의 정보를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
            }
    )
    @PutMapping("/{id}")
    public ProductDto.Res update(
            @Parameter(description = "수정할 상품 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "수정할 상품 정보")
            @Valid @RequestBody ProductDto.Update dto
    ) {
        return productService.update(id, dto);
    }

    @Operation(
            summary = "상품 할인율 변경",
            description = "특정 상품의 할인율만 변경합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "할인율 변경 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 할인율 값"),
                    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
            }
    )
    @PatchMapping("/{id}/discount-rate")
    public void changeDiscount(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "할인율 정보")
            @RequestBody DiscountReq req
    ) {
        productService.changeDiscountRate(id, req.rate());
    }

    // =================================================================
    // 상품 삭제 관련 API
    // =================================================================

    @Operation(
            summary = "상품 삭제",
            description = "특정 상품을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
            }
    )
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "삭제할 상품 ID", example = "1")
            @PathVariable Long id) {
        productService.delete(id);
    }

    // =================================================================
    // 연관 상품 추천 API
    // =================================================================

    @Operation(
            summary = "레시피 기반 연관 상품 조회",
            description = "레시피 재료를 기반으로 연관된 상품 16개를 추천합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "추천 성공"),
                    @ApiResponse(responseCode = "404", description = "레시피를 찾을 수 없음")
            }
    )
    @GetMapping("/recipe/{recipeId}/related")
    public ResponseEntity<BaseResponse<List<ProductDto.RelatedProductResponse>>> getRelatedProductsByRecipe(
            @Parameter(description = "레시피 ID", example = "1")
            @PathVariable Long recipeId) {

        List<ProductDto.RelatedProductResponse> relatedProducts =
                productService.getRelatedProductsByRecipe(recipeId);
        return ResponseEntity.ok(BaseResponse.success(relatedProducts));
    }

    // =================================================================
    // 요청 바디용 record
    // =================================================================

    public record PriceReq(
            @Min(value = 0, message = "판매가는 0원 이상이어야 합니다.")
            Integer price
    ) {}

    public record DiscountReq(
            @Min(value = 0, message = "할인율은 0 이상이어야 합니다.")
            @Max(value = 99, message = "할인율은 99% 이하여야 합니다.")
            Integer rate
    ) {}
}