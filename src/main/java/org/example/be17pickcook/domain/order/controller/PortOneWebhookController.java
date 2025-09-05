package org.example.be17pickcook.domain.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.order.service.PortOneWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 포트원(PortOne) 웹훅 컨트롤러
 * - 포트원 결제 시스템에서 발생하는 이벤트 처리
 * - 결제 취소, 환불 등의 웹훅 이벤트 수신
 * - 웹훅 서명 검증을 통한 보안 처리
 */
@Tag(name = "결제 웹훅", description = "포트원(PortOne) 결제 시스템의 웹훅 이벤트를 처리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PortOneWebhookController {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final PortOneWebhookService portOneWebhookService;

    // =================================================================
    // 웹훅 이벤트 처리 API
    // =================================================================

    @Operation(
            summary = "포트원 웹훅 이벤트 수신",
            description = "포트원 결제 시스템에서 발생하는 웹훅 이벤트를 수신하고 처리합니다. " +
                    "결제 취소, 환불 등의 이벤트가 발생할 때 자동으로 호출됩니다. " +
                    "이 API는 포트원에서만 호출되며, 일반 사용자는 직접 호출할 수 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "웹훅 처리 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 웹훅 데이터"),
                    @ApiResponse(responseCode = "401", description = "웹훅 서명 검증 실패"),
                    @ApiResponse(responseCode = "500", description = "웹훅 처리 중 서버 오류")
            }
    )
    @PostMapping("/webhook-portone")
    public ResponseEntity<Void> receiveWebhook(
            @Parameter(description = "포트원 웹훅 ID (보안 검증용)", required = true)
            @RequestHeader("webhook-id") String webhookId,
            @Parameter(description = "포트원 웹훅 서명 (보안 검증용)", required = true)
            @RequestHeader("webhook-signature") String webhookSignature,
            @Parameter(description = "포트원 웹훅 타임스탬프 (보안 검증용)", required = true)
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @Parameter(description = "포트원 웹훅 페이로드 (이벤트 데이터)", required = true)
            @RequestBody String payload) {

        System.out.println("[Webhook Payload] " + payload);

        portOneWebhookService.handleWebhookCancel(
                payload,
                webhookId,
                webhookSignature,
                webhookTimestamp
        );

        return ResponseEntity.ok().build();
    }
}