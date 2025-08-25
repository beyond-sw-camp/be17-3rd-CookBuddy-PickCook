package org.example.be17pickcook.domain.order.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class PortOneWebhookReqDto {


    /** 이벤트 타입 예: Transaction.Ready, Transaction.Paid, Transaction.Cancelled */
    private String type;

    /** 실제 이벤트 데이터 */
    private PortOneWebhookData data;

    /** DTO 내부 클래스: 결제 관련 데이터 */
    @Getter
    @NoArgsConstructor
    public static class PortOneWebhookData {

        /** PortOne 결제 ID */
        @JsonProperty("paymentId")
        private String paymentId;

        /** 결제 금액 */
        @JsonProperty("amount")
        private Amount amount;

        /** 결제 수단 */
        @JsonProperty("method")
        private String method;

        /** 주문 ID 등 추가 정보를 Map으로 받을 수도 있음 */
        @JsonProperty("metadata")
        private Map<String, Object> metadata;

        /** 내부 클래스: 금액 정보 */
        @Getter
        @NoArgsConstructor
        public static class Amount {
            @JsonProperty("total")
            private Long total;

            @JsonProperty("vat")
            private Long vat;

            @JsonProperty("tax_free")
            private Long taxFree;
        }
    }
}
