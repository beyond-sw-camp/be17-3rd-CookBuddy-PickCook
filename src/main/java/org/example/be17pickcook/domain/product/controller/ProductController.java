package org.example.be17pickcook.domain.product.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
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

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ================== 등록 ==================
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @RequestPart ProductDto.Register dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws SQLException, IOException {
        productService.register(authUser, dto, files);
        return ResponseEntity.status(200).body("상품 등록 성공!");
    }

    @GetMapping("list-with-reviews")
    public ResponseEntity<Page<ProductDto.Response>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "productId") String sortBy
    ) {
        Page<ProductDto.Response> productPage = productService.getPagedProductsWithReviewsDto(page, size, sortBy);
        return ResponseEntity.ok(productPage);
    }




    // ================== 전체 목록 조회 (페이징 + 정렬) ==================
    @GetMapping
    public ResponseEntity<Page<ProductDto.Res>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,   // [변경] 기본 정렬 기준을 createdAt → id
            @RequestParam(defaultValue = "ASC") String dir    // [변경] 기본 정렬 방향을 DESC → ASC
    ) {
        Sort s = dir.equalsIgnoreCase("DESC")
                ? Sort.by(sort).descending()
                : Sort.by(sort).ascending();
        Pageable pageable = PageRequest.of(page, size, s);
        return ResponseEntity.ok(productService.findAll(pageable));
    }

    // ================== 전체 목록 조회 (전부 반환 + 정렬만) ==================
    @GetMapping("/list")
    public BaseResponse<List<ProductDto.Res>> findAllNoPaging(
            @RequestParam(defaultValue = "id") String sort,   // [변경] createdAt → id
            @RequestParam(defaultValue = "ASC") String dir    // [변경] DESC → ASC
    ) {
        Sort s = dir.equalsIgnoreCase("DESC")
                ? Sort.by(sort).descending()
                : Sort.by(sort).ascending();
        return BaseResponse.success(productService.findAllNoPaging(s));
    }

    // ================== 단건 조회 ==================
    @GetMapping("/{id}")
    public ProductDto.Res findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    // ================== 수정 ==================
    @PutMapping("/{id}")
    public ProductDto.Res update(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto.Update dto
    ) {
        return productService.update(id, dto);
    }

    // ================== 할인율만 변경 ==================
    @PatchMapping("/{id}/discount-rate")
    public void changeDiscount(
            @PathVariable Long id,
            @RequestBody DiscountReq req
    ) {
        productService.changeDiscountRate(id, req.rate());
    }

    // ================== 삭제 ==================
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    // ================== 요청 바디용 record ==================
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
