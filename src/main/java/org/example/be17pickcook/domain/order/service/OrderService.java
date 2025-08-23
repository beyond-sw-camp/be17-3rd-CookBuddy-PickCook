package org.example.be17pickcook.domain.order.service;

import io.portone.sdk.server.payment.PaidPayment;
import io.portone.sdk.server.payment.Payment;
import io.portone.sdk.server.payment.PaymentClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void updateOrderStatus(String paymentId, OrderStatus newStatus) {
        Orders order = orderRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        // 상태 전환 규칙 적용 가능
        switch (newStatus) {
            case PAID:
                if (order.getStatus() == OrderStatus.PENDING) {
                    order.updateStatus(OrderStatus.PAID);
                }
                break;
            case FAILED:
                if (order.getStatus() == OrderStatus.PENDING) {
                    order.updateStatus(OrderStatus.FAILED);
                }
                break;
            case CANCELED:
                if (order.getStatus() == OrderStatus.PENDING) {
                    order.updateStatus(OrderStatus.CANCELED);
                }
                break;
            case REFUNDED:
                if (order.getStatus() == OrderStatus.PAID) {
                    order.updateStatus(OrderStatus.REFUNDED);
                }
                break;
            default:
                break;
        }
    }

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
                if (order.getOrderItems() != null) {
                    order.getOrderItems().clear();
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

//
//    @Transactional
//    public Boolean validation(OrderDto.PaymentValidationReqDto dto) throws JsonProcessingException {
//        System.out.println("validation 시작: " + dto.getPaymentId());
//
//        // PortOne v2 결제 조회 URL
//        String url = "https://api.portone.io/payments/" + dto.getPaymentId() +
//                "?storeId=store-018bff32-3d9e-4918-9f0a-add338f287cd"; // storeId 쿼리 추가
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + "eyJraWQiOiI2YmZhMWMzYy02N2JjLTQ2N2YtYjdlYy01ODc4M2YwYjc3MWMiLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJ1c2VyX2lkIjoidXNlci01ZDg1YjA1Yy00OTUwLTQ2OTUtYjZmOS1jODRmMGM3NDE1YmUiLCJpc3MiOiJJQU1QT1JUIiwiZXhwIjoxNzU1OTc4NDA2LCJjdXN0b21fcGF5bG9hZCI6eyJtZXJjaGFudF9pZCI6Im1lcmNoYW50LWFlMzMxZWQxLTEwOGMtNDM4MS05MjM3LTZmZDNlYjJiNDU4YiIsInN0b3JlX2lkIjoic3RvcmUtMDE4YmZmMzItM2Q5ZS00OTE4LTlmMGEtYWRkMzM4ZjI4N2NkIn0sIm1lcmNoYW50X3NlcnZpY2UiOnsiaW5jbHVkZV9wZXJtaXNzaW9ucyI6dHJ1ZSwibWVyY2hhbnRfaWQiOiJtZXJjaGFudC1hZTMzMWVkMS0xMDhjLTQzODEtOTIzNy02ZmQzZWIyYjQ1OGIiLCJzdG9yZV9pZCI6InN0b3JlLTAxOGJmZjMyLTNkOWUtNDkxOC05ZjBhLWFkZDMzOGYyODdjZCIsImJlbG9uZ190byI6Ik1FUkNIQU5UIiwicGVybWlzc2lvbnMiOlsiVFhfUkVBRCIsIlRYX1VQREFURSIsIlJFQ09OX1JFQUQiLCJSRUNPTl9VUERBVEUiLCJQTEFURk9STV9TRVRUTEVNRU5UX1BPTElDWV9SRUFEIiwiUExBVEZPUk1fU0VUVExFTUVOVF9QT0xJQ1lfV1JJVEUiLCJQTEFURk9STV9QQVJUTkVSX1JFQUQiLCJQTEFURk9STV9QQVJUTkVSX1dSSVRFIiwiUExBVEZPUk1fVFJBTlNGRVJfUkVBRCIsIlBMQVRGT1JNX1RSQU5TRkVSX1dSSVRFIiwiUExBVEZPUk1fUEFSVE5FUl9TRVRUTEVNRU5UX1JFQUQiLCJQTEFURk9STV9QQVJUTkVSX1NFVFRMRU1FTlRfV1JJVEUiLCJQTEFURk9STV9QQVlPVVRfUkVBRCIsIlBMQVRGT1JNX0JVTEtfUEFZT1VUX1JFQUQiLCJQTEFURk9STV9FWENFTF9ET1dOTE9BRCJdLCJ3aGl0ZWxpc3QiOltdfSwiaWF0IjoxNzU1OTc2NjA2fQ.dUGvAh7oozhk2bvyt_UpPA_LdKy0QkBbIGXt06lsIiPX22WL8UQOvJY6WQMsUQdeZD8KeTOAQwn4OYlIBIOEdw"); // Bearer 형식 사용
//        headers.set("Accept", "application/json");
//
//        HttpEntity<Void> entity = new HttpEntity<>(headers);
//
//        try {
//            ResponseEntity<String> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.GET,
//                    entity,
//                    String.class
//            );
//
//            if (response.getStatusCode().is2xxSuccessful()) {
//                System.out.println("포트원 결제 조회 완료: " + response.getBody());
//
//                JsonNode root = objectMapper.readTree(response.getBody());
//                JsonNode payment = root; // 최상위 노드 사용
//
//
//                String status = payment.path("status").asText();
//                int paidAmount = payment.path("amount").path("total").asInt();
//
//                System.out.println("결제 상태: " + status + ", 금액: " + paidAmount);
//
//                // DB 데이터 조회
//                Orders order = orderRepository.findByPaymentId(dto.getPaymentId())
//                        .orElseThrow(() -> new IllegalArgumentException("해당 주문 없음"));
//
//                if (order.getTotal_price() == paidAmount) {
//                    System.out.println("결제 금액 검증 완료 ✅");
//                    order.updateStatus(OrderStatus.PAID);
//                    return true;
//                } else {
//                    System.out.println("❌ 금액 불일치 - DB: " + order.getTotal_price() + ", PortOne: " + paidAmount);
//                }
//
//            } else {
//                System.out.println("포트원 결제 조회 실패: " + response.getStatusCode());
//            }
//        } catch (Exception e) {
//            System.out.println("포트원 결제 조회 중 오류: " + e.getMessage());
//        }
//
//        return false;
//    }
//

}
