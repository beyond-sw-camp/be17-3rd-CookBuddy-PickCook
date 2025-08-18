package org.example.be17pickcook.domain.product.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.product.model.ProductDto;
import org.example.be17pickcook.domain.product.service.ProductService;
import org.example.be17pickcook.domain.recipe.model.RecipeDto;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    // 등록
    @PostMapping("/register")
    public ResponseEntity register(@AuthenticationPrincipal UserDto.AuthUser authUser,
                                   @RequestPart ProductDto.Register dto,
                                   @RequestPart(value = "files", required = false)List<MultipartFile> files) throws SQLException, IOException {

        productService.register(authUser,dto,files);

        return ResponseEntity.status(200).body("상품 등록 성공!");
    }

    // 전체 조회
    @GetMapping
    public List<ProductDto.Res> findAll() {
        return productService.findAll();
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ProductDto.Res findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    // 수정(전체/부분은 DTO 내부 apply 로 처리)
    @PutMapping("/{id}")
    public ProductDto.Res update(@PathVariable Long id,
                                 @Valid @RequestBody ProductDto.Update dto) {
        return productService.update(id, dto);
    }

    // 가격만 변경 -> service.changePrice 사용됨
    @PatchMapping("/{id}/price")
    public void changePrice(@PathVariable Long id,
                            @RequestBody PriceReq req) {
        productService.changePrice(id, req.price());
    }

    // 할인율만 변경 -> service.changeDiscountRate 사용됨
    @PatchMapping("/{id}/discount-rate")
    public void changeDiscount(@PathVariable Long id,
                               @RequestBody DiscountReq req) {
        productService.changeDiscountRate(id, req.rate());
    }

    // 삭제
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    // 요청 바디용 record (검증 포함)
    public record PriceReq(
            @Min(value = 0, message = "판매가는 0원 이상이어야 합니다.")
            Integer price) {}

    public record DiscountReq(
            @Min(value = 0, message = "할인율은 0 이상이어야 합니다.")
            @Max(value = 99, message = "할인율은 99% 이하여야 합니다.")
            Integer rate) {}
}
