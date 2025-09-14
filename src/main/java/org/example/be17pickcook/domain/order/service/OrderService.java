package org.example.be17pickcook.domain.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.portone.sdk.server.payment.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.PageResponse;
import org.example.be17pickcook.domain.cart.repository.CartsRepository;
import org.example.be17pickcook.domain.order.model.OrderItem;
import org.example.be17pickcook.domain.order.model.OrderStatus;
import org.example.be17pickcook.domain.order.model.Orders;
import org.example.be17pickcook.domain.order.model.OrderDto;
import org.example.be17pickcook.domain.order.repository.OrderItemRepository;
import org.example.be17pickcook.domain.order.repository.OrderRepository;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.product.model.ProductDto;
import org.example.be17pickcook.domain.review.repository.ReviewRepository;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartsRepository cartsRepository;
    private final ReviewRepository reviewRepository;
    private final EntityManager entityManager;


    @Value("${portone.secret-key}")
    private String portoneSecretKey;
    @Value("${portone.store-id}")
    private String portoneStoreId;

    // 고객용 주문번호 만드는 함수
    private String generateOrderNumber() {
        LocalDate today = LocalDate.now();
        String datePart = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%05d", new Random().nextInt(100000));
        return "ORD" + datePart + "-" + randomPart;
    }

    // 주문 요청 기록 저장
    @Transactional
    public OrderDto.PaymentStartResDto startPayment(UserDto.AuthUser authUser,
                                                    OrderDto.PaymentStartReqDto dto) {

        String paymentId = UUID.randomUUID().toString();
        String orderNumber = generateOrderNumber();
        User user = User.builder().idx(authUser.getIdx()).build();

        Orders order = dto.toEntity(user, paymentId);
        order.updateOrderNumber(orderNumber);
        orderRepository.save(order);

        return new OrderDto.PaymentStartResDto(paymentId, order.getStatus().name());
    }

    @Transactional
    public OrderDto.PaymentValidationResDto validation (OrderDto.PaymentValidationReqDto dto) {
        try {
            // paymentId로 포트원 결제 조회
            PaymentClient paymentClient = new PaymentClient(
                    portoneSecretKey,
                    "https://api.portone.io",
                    portoneStoreId
            );

            log.debug("포트원 결제 조회 시작: {}", dto.getPaymentId());

            Payment payment = paymentClient.getPayment(dto.getPaymentId())
                    .get(15, TimeUnit.SECONDS);

            log.debug("포트원 결제 조회 완료: {}", payment);

            // DB 주문 조회
            Orders order = orderRepository.findByPaymentId(dto.getPaymentId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 주문 없음"));

            // 이미 최종 상태면 바로 반환
            if (order.getStatus() == OrderStatus.CANCELED ||
                    order.getStatus() == OrderStatus.REFUNDED ||
                    order.getStatus() == OrderStatus.PAID) {
                log.debug("이미 최종 상태 주문: {}", order.getStatus());
                return new OrderDto.PaymentValidationResDto(order.getIdx(), order.getStatus().name());
            }

            // 결제 완료 상태인지 확인
            if (!(payment instanceof PaidPayment paidPayment)) {
                order.updateStatus(OrderStatus.FAILED);
                log.debug("결제 완료 상태 아님: {}", payment != null ? payment.getClass().getSimpleName() : "null");
                return new OrderDto.PaymentValidationResDto(order.getIdx(), OrderStatus.FAILED.name());
            }

            // 금액 검증
            Long paidAmount = paidPayment.getAmount().getTotal();
            int totalPrice = order.getTotal_price();

            if (paidAmount != null && paidAmount.equals((long) totalPrice)) {
                // 금액 일치 → 결제 완료
                order.updateStatus(OrderStatus.PAID);

                // 장바구니 항목 삭제
                if ("CART".equals(order.getOrderType()) && order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                    for (OrderItem item : order.getOrderItems()) {
                        if (item.getProduct() != null && order.getUser() != null) {
                            // Cart 삭제: userId + productId 기준
                            cartsRepository.deleteByUserAndProduct(order.getUser().getIdx(), item.getProduct().getId());
                            log.debug("장바구니 삭제 완료, productId: {}", item.getProduct().getId());
                        }
                    }
                }

                // 결제수단 가져오기
                PaymentMethod method = paidPayment.getMethod();
                String paymentProvider = "Unknown";
                ObjectMapper objectMapper = new ObjectMapper();

                // 간편 결제일 경우
                try {
                    String pgResponseJson = paidPayment.getPgResponse(); // pgResponse JSON 문자열
                    if (pgResponseJson != null) {
                        JsonNode root = objectMapper.readTree(pgResponseJson);
                        JsonNode easyPayNode = root.path("easyPay");
                        if (!easyPayNode.isMissingNode()) {
                            JsonNode providerNode = easyPayNode.path("provider");
                            if (!providerNode.isMissingNode()) {
                                paymentProvider = providerNode.asText(); // 여기서 "카카오페이" 가져옴
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("결제수단 조회 실패", e);
                }

                // 카드 결제일 경우
                if (method instanceof PaymentMethodCard) {
                    PaymentMethodCard cardMethod = (PaymentMethodCard) method;
                    if (cardMethod.getCard() != null && cardMethod.getCard().getName() != null) {
                        paymentProvider = cardMethod.getCard().getName(); // 카드사 이름
                    }
                }

                // 결제 완료 시간 타입 변환하기
                LocalDateTime paidAtKst = paidPayment.getPaidAt()
                        .atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                        .toLocalDateTime();

                order.updatePaymentMethod(paymentProvider);
                order.updateApproveAt(paidAtKst);

                return new OrderDto.PaymentValidationResDto(order.getIdx(), OrderStatus.PAID.name());
            } else {
                // 금액 불일치 → 실패
                order.updateStatus(OrderStatus.FAILED);
                log.debug("❌ 금액 불일치 - DB: {}, PortOne: {}", totalPrice, paidAmount);
                return new OrderDto.PaymentValidationResDto(order.getIdx(), OrderStatus.FAILED.name());
            }

        } catch (ExecutionException e) {
            log.error("포트원 결제 조회 실패", e.getCause());
            updateOrderStatusToFailed(dto.getPaymentId());
            return new OrderDto.PaymentValidationResDto(null, OrderStatus.FAILED.name());
        } catch (TimeoutException e) {
            log.error("포트원 결제 조회 타임아웃", e);
            updateOrderStatusToFailed(dto.getPaymentId());
            return new OrderDto.PaymentValidationResDto(null, OrderStatus.FAILED.name());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("포트원 결제 조회 중 인터럽트 발생", e);
            updateOrderStatusToFailed(dto.getPaymentId());
            return new OrderDto.PaymentValidationResDto(null, OrderStatus.FAILED.name());
        } catch (Exception e) {
            log.error("포트원 결제 조회 중 오류 발생", e);
            updateOrderStatusToFailed(dto.getPaymentId());
            return new OrderDto.PaymentValidationResDto(null, OrderStatus.FAILED.name());
        }
    }


    private void updateOrderStatusToFailed(String paymentId) {
        orderRepository.findByPaymentId(paymentId).ifPresent(order -> {
            order.updateStatus(OrderStatus.FAILED);
            log.debug("결제 검증 실패로 주문 상태 FAILED 업데이트, paymentId={}", paymentId);
        });
    }


    // 주문 상품 조회(단일)
    public OrderDto.OrderInfoDto getOrderInfo(Integer userIdx, Long productIdx, Long orderIdx) {
        System.out.println("실행은 됟고 있어??");
        OrderItem orderItem = orderItemRepository.findByProductIdAndOrderId(productIdx, orderIdx)
                .orElseThrow(() -> new IllegalArgumentException("주문 상품을 찾을 수 없습니다."));

        return OrderDto.OrderInfoDto.fromEntity(orderItem);
    }


    // 주문 목록
    public PageResponse<OrderDto.OrderInfoListDto> getOrdersByPeriodPaged(String period, int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;

        switch (period) {
            case "3M": start = now.minusMonths(3); break;
            case "6M": start = now.minusMonths(6); break;
            case "1Y": start = now.minusYears(1); break;
            case "3Y": start = now.minusYears(3); break;
            default: start = now.minusMonths(3);
        }

        Pageable pageable = PageRequest.of(page, size);
        List<OrderStatus> statuses = List.of(OrderStatus.PAID, OrderStatus.REFUNDED);

        Page<Orders> ordersPage = orderRepository.findAllWithItemsByCreatedAtBetweenAndStatuses(
                start,
                now,
                statuses,
                pageable
        );

        // DTO 변환
        List<OrderDto.OrderInfoListDto> pageList = ordersPage.stream()
                .map(OrderDto.OrderInfoListDto::fromEntity) // 여기서 OrderInfoListDto 생성
                .toList();

        Page<OrderDto.OrderInfoListDto> dtoPage = new PageImpl<>(pageList, pageable, ordersPage.getTotalElements());

        return PageResponse.from(dtoPage);
    }


    // 주문 상세 조회
    public OrderDto.OrderDetailDto getOrderDetail(Integer userIdx, Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        // 주문한 사용자와 현재 로그인한 사용자 비교
        if (!order.getUser().getIdx().equals(userIdx)) {
            throw new RuntimeException("본인 주문만 조회할 수 있습니다.");
        }

        List<OrderDto.OrderInfoDto> orderItemsWithReview = order.getOrderItems().stream()
                .map(oi -> {
                    boolean hasReview = reviewRepository.existsReviewByOrderAndProduct(
                            order.getIdx(), oi.getProduct().getId()
                    );
                    return OrderDto.OrderInfoDto.fromEntity(oi, hasReview);
                })
                .toList();

        return OrderDto.OrderDetailDto.fromEntity(order, orderItemsWithReview);
    }
}