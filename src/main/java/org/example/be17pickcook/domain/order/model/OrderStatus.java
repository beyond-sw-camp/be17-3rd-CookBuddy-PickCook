package org.example.be17pickcook.domain.order.model;

public enum OrderStatus {
    PENDING, // 보류 (사용자가 결제하려는 거 확인했지만 아직 결제가 진행되지는 않음.
    PAID, // 결제 완료
    FAILED, // 결제 실패 (결제 중간에 오류가 발생해서 실패함)
    CANCELED, // 결제 취소 (사용자가 결제하려다가 중간에 취소함)
    REFUNDED // 환불
}
