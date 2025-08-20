package org.example.be17pickcook.domain.cart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.domain.cart.model.CartsDto;
import org.example.be17pickcook.domain.cart.service.CartsService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
@Tag(name = "장바구니 기능", description = "장바구니 등록, 제거, 목록 조회 기능을 제공합니다.")
public class CartsController {
    private final CartsService cartsService;

    @Operation(
            summary = "장바구니 등록",
            description = "사용자가 상품을 장바구니에 등록합니다.\n"
    )
    @PostMapping("/register")
    public ResponseEntity inCart(@AuthenticationPrincipal UserDto.AuthUser authUser,
                                   @RequestBody CartsDto.CartsRequestDto dto) {
        cartsService.toggleInCart(authUser, dto);

        return ResponseEntity.status(200).body("장바구니 기능 동작 성공");
    }


    @Operation(
            summary = "장바구니 항목 조회",
            description = "사용자의 장바구니 정보를 조회합니다."
    )
    @GetMapping
    public BaseResponse<List<CartsDto.CartsResponseDto>> getCartList(@AuthenticationPrincipal UserDto.AuthUser authUser) {
        Integer userIdx = (authUser != null) ? authUser.getIdx() : null;

        return BaseResponse.success(cartsService.getCarts(userIdx));
    }


    // 장바구니 항목 제거
}
