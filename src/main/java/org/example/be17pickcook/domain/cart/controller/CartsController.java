package org.example.be17pickcook.domain.cart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.domain.cart.model.CartsDto;
import org.example.be17pickcook.domain.cart.service.CartsService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
@Tag(name = "장바구니 관리", description = "장바구니 등록, 제거, 목록 조회 기능을 제공합니다.")
public class CartsController {
    private final CartsService cartsService;

    @Operation(
            summary = "장바구니 등록",
            description = "사용자가 상품을 장바구니에 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @PostMapping("/register")
    public BaseResponse<String> register(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "장바구니 등록 요청 정보", required = true)
            @RequestBody CartsDto.CartsRequestDto dto) {
        cartsService.register(authUser, dto);
        return BaseResponse.success("장바구니에 상품 등록 성공");
    }

    @Operation(
            summary = "장바구니 삭제",
            description = "사용자가 상품을 장바구니에서 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @PostMapping("/delete")
    public BaseResponse<String> delete(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "장바구니 삭제 요청 정보", required = true)
            @RequestBody CartsDto.CartsDeleteDto dto) {
        cartsService.delete(authUser, dto);
        return BaseResponse.success("장바구니에 상품 삭제 성공");
    }

    @Operation(
            summary = "장바구니 항목 조회",
            description = "사용자의 장바구니 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping
    public BaseResponse<List<CartsDto.CartsResponseDto>> getCartList(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser) {
        Integer userIdx = (authUser != null) ? authUser.getIdx() : null;
        return BaseResponse.success(cartsService.getCarts(userIdx));
    }

    @Operation(
            summary = "장바구니 수량 변경",
            description = "특정 장바구니 항목의 수량을 변경합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수량 변경 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "장바구니 항목을 찾을 수 없음")
            }
    )
    @PatchMapping("/{id}")
    public BaseResponse<String> updateQuantity(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "장바구니 아이템 ID", example = "1")
            @PathVariable("id") Long cartItemId,
            @Parameter(description = "수량 변경 요청 정보", required = true)
            @RequestBody CartsDto.CartQuantityUpdateRequest dto) {
        cartsService.updateQuantity(authUser, cartItemId, dto.getQuantity());
        return BaseResponse.success("수량이 변경되었습니다.");
    }
}