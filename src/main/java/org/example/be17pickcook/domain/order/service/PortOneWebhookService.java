package org.example.be17pickcook.domain.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.domain.order.model.OrderStatus;
import org.example.be17pickcook.domain.order.model.PortOneWebhookReqDto;
import org.example.be17pickcook.domain.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortOneWebhookService {
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @Value("${PORTONE_WEBHOOK_SECRET}")
    private String WEBHOOK_SECRET;

    private final String HMAC_SHA256 = "HmacSHA256";
    private static final String SECRET_PREFIX = "whsec_";


    public void handleWebhookCancel(String payload, String webhookId, String webhookSignature, String webhookTimestamp) {
        // 1. 웹훅 검증
        verifyTimestamp(webhookTimestamp);
        String expectedSignature = generateSignature(webhookId, webhookTimestamp, payload);
        verifySignature(expectedSignature, webhookSignature);

        // 2. Payload 파싱
        PortOneWebhookReqDto webhookReqDTO = parsePayload(payload);

        // 3. 취소 이벤트만 처리 (결제가 완전 취소되었을 때)
        if ("Transaction.Cancelled".equals(webhookReqDTO.getType())) {
            String paymentId = webhookReqDTO.getData().getPaymentId();
            orderRepository.findByPaymentId(paymentId).ifPresent(order -> {
                if (order.getStatus() != OrderStatus.CANCELED) {
                    order.updateStatus(OrderStatus.CANCELED);
                    log.info("[Webhook] 주문 취소 처리 완료 - paymentId={}", paymentId);
                }
            });
        }
    }

    // ---------------------- 내부 메서드 ----------------------


    /** Webhook 타임스탬프 검증 (5분 유효) */
    private void verifyTimestamp(String timestamp) {
        // timestamp 검증 로직 구현
        long now = System.currentTimeMillis() / 1000; // 현재 시간 (seconds)
        long requestTimestamp = Long.parseLong(timestamp);
        if (Math.abs(now - requestTimestamp) > 300) { // 5분
            log.info("[PortOne Webhook] 유효하지 않은 타임스탬프: {}", timestamp);
            throw new IllegalArgumentException("[PortOne Webhook] 유효하지 않은 타임스탬프입니다.");
        }
    }

    /** HMAC-SHA256 + Base64 서명 생성 */
    private String generateSignature (String webhookId, String timestamp, String payload) {
        try {
            String secret = WEBHOOK_SECRET.startsWith(SECRET_PREFIX)
                    ? WEBHOOK_SECRET.substring(SECRET_PREFIX.length())
                    : WEBHOOK_SECRET;

            byte[] decodedKey = Base64.getDecoder().decode(secret);
            SecretKeySpec keySpec = new SecretKeySpec(decodedKey, HMAC_SHA256);

            String toSign = webhookId + "." + timestamp + "." + payload;

            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(keySpec);
            byte[] macData = mac.doFinal(toSign.getBytes(StandardCharsets.UTF_8));

            return "v1," + Base64.getEncoder().encodeToString(macData);
        } catch (Exception e) {
            throw new RuntimeException("시그니처 생성 중 오류 발생", e);
        }
    }

    /** 서명 검증 */
    private void verifySignature (String expectedSignature, String webhookSignature){
        if (!expectedSignature.equals(webhookSignature)) {
            log.warn("[PortOne Webhook] 시그니처 검증 실패: expected={}, webhookSignature={}",
                    expectedSignature, webhookSignature);
            throw new IllegalArgumentException("유효하지 않은 시그니처입니다.");
        }
    }

    /** 페이로드 JSON -> DTO 변환 */
    private PortOneWebhookReqDto parsePayload (String payload){
        try {
            System.out.println(objectMapper.readValue(payload, PortOneWebhookReqDto.class));
            return objectMapper.readValue(payload, PortOneWebhookReqDto.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("웹훅 payload 파싱 실패", e);
        }
    }
}