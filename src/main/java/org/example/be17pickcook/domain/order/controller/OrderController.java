package org.example.be17pickcook.domain.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.PageResponse;
import org.example.be17pickcook.domain.order.model.OrderDto;
import org.example.be17pickcook.domain.order.model.RefundDto;
import org.example.be17pickcook.domain.order.service.OrderService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 주문 관리 컨트롤러
 * - 결제 시작 및 검증 API
 * - 주문 내역 조회 API
 * - 포트원(PortOne) 결제 시스템 연동
 * - 주문 상세 정보 조회
 */
@Tag(name = "주문 관리", description = "주문하기, 주문 기록 조회 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final OrderService orderService;

    // =================================================================
    // 결제 관련 API
    // =================================================================

    @Operation(
            summary = "결제 시작",
            description = "주문 결제를 시작합니다. 포트원(PortOne) 결제 시스템을 통해 결제 준비 단계를 진행합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "결제 시작 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 주문 정보"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "500", description = "결제 시스템 오류")
            }
    )
    @PostMapping("/start")
    public BaseResponse<OrderDto.PaymentStartResDto> startPayments(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "결제 시작 요청 정보", required = true)
            @RequestBody OrderDto.PaymentStartReqDto dto) {

        OrderDto.PaymentStartResDto response = orderService.startPayment(authUser, dto);
        return BaseResponse.success(response);
    }

    @Operation(
            summary = "결제 검증",
            description = "포트원에서 받은 결제 결과를 서버에서 검증합니다. 결제 완료 후 주문 상태를 업데이트합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "결제 검증 성공"),
                    @ApiResponse(responseCode = "400", description = "결제 검증 실패"),
                    @ApiResponse(responseCode = "409", description = "이미 처리된 결제")
            }
    )
    @PostMapping("/validation")
    public BaseResponse<OrderDto.PaymentValidationResDto> validation(
            @Parameter(description = "결제 검증 요청 정보", required = true)
            @RequestBody OrderDto.PaymentValidationReqDto dto) {

        OrderDto.PaymentValidationResDto response = orderService.validation(dto);
        return BaseResponse.success(response);
    }

    // =================================================================
    // 주문 조회 관련 API
    // =================================================================

    @Operation(
            summary = "주문 내역 조회 (기간별)",
            description = "특정 기간의 주문 내역을 페이징하여 조회합니다. 기간별 필터링과 페이징을 지원합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 기간 파라미터"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/history")
    public BaseResponse<PageResponse<OrderDto.OrderInfoListDto>> getOrdersByPeriod(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "조회 기간 (예: 1month, 3months, 6months, 1year)", example = "1month")
            @RequestParam String period,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam int page,
            @Parameter(description = "페이지당 주문 수", example = "10")
            @RequestParam int size) {

        Integer userIdx = authUser.getIdx();

        PageResponse<OrderDto.OrderInfoListDto> result = orderService.getOrdersByPeriodPaged(userIdx, period, page, size);
        return BaseResponse.success(result);
    }

    @Operation(
            summary = "주문 상세 조회",
            description = "특정 주문의 상세 정보를 조회합니다. 주문한 상품 목록, 배송 정보, 결제 정보 등을 포함합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음 (다른 사용자의 주문)"),
                    @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
            }
    )
    @GetMapping("/details")
    public BaseResponse<OrderDto.OrderDetailDto> getOrderDetail(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "조회할 주문 ID", required = true, example = "1")
            @RequestParam Long orderId) {

        Integer userIdx = (authUser != null) ? authUser.getIdx() : null;
        OrderDto.OrderDetailDto result = orderService.getOrderDetail(userIdx, orderId);
        return BaseResponse.success(result);
    }


    @Operation(
            summary = "리뷰 작성할 상품 조회",
            description = "리뷰를 작성할 상품의 정보를 조회합니다."
    )
    @GetMapping("/product")
    public BaseResponse<OrderDto.OrderInfoDto> getOrderProduct(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "조회할 상품 ID", required = true, example = "1")
            @RequestParam Long productId,
            @Parameter(description = "조회할 주문 ID", required = true, example = "1")
            @RequestParam Long orderId) {

        Integer userIdx = (authUser != null) ? authUser.getIdx() : null;
        OrderDto.OrderInfoDto result = orderService.getOrderInfo(userIdx, productId, orderId);
        return BaseResponse.success(result);
    }


    @Operation(
            summary = "환불",
            description = "결제 취소 (전액/부분)"
    )
    @PostMapping("/refund")
    public BaseResponse cancelPayment(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @RequestBody RefundDto.RefundRequestDto requestDto
    ) {
        RefundDto.RefundResponseDto response = orderService.refundPayment(requestDto, authUser.getIdx());
        return BaseResponse.success("환불 성공");
    }
}