package org.example.be17pickcook.domain.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.PageResponse;
import org.example.be17pickcook.domain.order.model.OrderDto;
import org.example.be17pickcook.domain.order.model.OrderStatus;
import org.example.be17pickcook.domain.order.model.Orders;
import org.example.be17pickcook.domain.order.service.OrderService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
@Tag(name = "주문 기능", description = "주문하기, 주문 기록 조회 기능을 제공합니다.")
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/start")
    public BaseResponse startPayments(@AuthenticationPrincipal UserDto.AuthUser authUser,
                                      @RequestBody OrderDto.PaymentStartReqDto dto) {

        OrderDto.PaymentStartResDto response = orderService.startPayment(authUser, dto);

        return BaseResponse.success(response);
    }

    @PostMapping("/validation")
    public BaseResponse validation(@RequestBody OrderDto.PaymentValidationReqDto dto) {
        OrderDto.PaymentValidationResDto response = orderService.validation(dto);
        return BaseResponse.success(response);
    }


    @GetMapping("/history")
    public BaseResponse<PageResponse<OrderDto.OrderInfoListDto>> getOrdersByPeriod(
            @RequestParam String period,
            @RequestParam int page,
            @RequestParam int size) {

        PageResponse<OrderDto.OrderInfoListDto> result = orderService.getOrdersByPeriodPaged(period, page, size);
        return BaseResponse.success(result);
    }

    @GetMapping("/details")
    public BaseResponse<OrderDto.OrderDetailDto> getOrderDetail(@AuthenticationPrincipal UserDto.AuthUser authUser,
                                                              @RequestParam Long orderId) {
        Integer userIdx = (authUser != null) ? authUser.getIdx() : null;
        OrderDto.OrderDetailDto result = orderService.getOrderDetail(userIdx, orderId);

        return BaseResponse.success(result);
    }
}
