package org.example.be17pickcook.domain.order.controller;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.order.service.PortOneWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class PortOneWebhookController {
    private final PortOneWebhookService portOneWebhookService;

    @PostMapping("/webhook-portone")
    public ResponseEntity receiveWebhook(
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-signature") String webhookSignature,
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @RequestBody String payload
    ) {
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
