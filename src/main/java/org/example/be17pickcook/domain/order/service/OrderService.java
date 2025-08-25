package org.example.be17pickcook.domain.order.service;

import io.portone.sdk.server.payment.PaidPayment;
import io.portone.sdk.server.payment.Payment;
import io.portone.sdk.server.payment.PaymentClient;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.domain.cart.repository.CartsRepository;
import org.example.be17pickcook.domain.order.model.OrderItem;
import org.example.be17pickcook.domain.order.model.OrderStatus;
import org.example.be17pickcook.domain.order.model.Orders;
import org.example.be17pickcook.domain.order.model.OrderDto;
import org.example.be17pickcook.domain.order.repository.OrderRepository;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartsRepository cartsRepository;
    private final EntityManager entityManager;


    @Value("${portone.secret-key}")
    private String portoneSecretKey;
    @Value("${portone.store-id}")
    private String portoneStoreId;

    // 주문 요청 기록 저장
    @Transactional
    public OrderDto.PaymentStartResDto startPayment(UserDto.AuthUser authUser,
                                                    OrderDto.PaymentStartReqDto dto) {

        String paymentId = UUID.randomUUID().toString();
        User user = User.builder().idx(authUser.getIdx()).build();

        Orders order = dto.toEntity(user, paymentId);
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
                if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                    for (OrderItem item : order.getOrderItems()) {
                        if (item.getProduct() != null && order.getUser() != null) {
                            // Cart 삭제: userId + productId 기준
                            cartsRepository.deleteByUserAndProduct(order.getUser().getIdx(), item.getProduct().getId());
                            log.debug("장바구니 삭제 완료, productId: {}", item.getProduct().getId());
                        }
                    }
                }

                log.debug("결제 금액 검증 완료 ✅ DB: {}, PortOne: {}", totalPrice, paidAmount);
                return new OrderDto.PaymentValidationResDto(order.getIdx(), OrderStatus.PAID.name());
            } else {
                // 금액 불일치 → 실패
                order.updateStatus(OrderStatus.FAILED);
                log.debug("❌ 금액 불일치 - DB: {}, PortOne: {}", totalPrice, paidAmount);
                return new OrderDto.PaymentValidationResDto(order.getIdx(), OrderStatus.FAILED.name());
            }

        } catch (ExecutionException e) {
            log.error("포트원 결제 조회 실패", e.getCause());
            return new OrderDto.PaymentValidationResDto(null, OrderStatus.FAILED.name());
        } catch (TimeoutException e) {
            log.error("포트원 결제 조회 타임아웃", e);
            return new OrderDto.PaymentValidationResDto(null, OrderStatus.FAILED.name());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("포트원 결제 조회 중 인터럽트 발생", e);
            return new OrderDto.PaymentValidationResDto(null, OrderStatus.FAILED.name());
        } catch (Exception e) {
            log.error("포트원 결제 조회 중 오류 발생", e);
            return new OrderDto.PaymentValidationResDto(null, OrderStatus.FAILED.name());
        }
    }
}