package org.example.be17pickcook.domain.refrigerator.enums;

import lombok.Getter;

// SyncPromptMessage.java (새 파일)
@Getter
public enum SyncPromptMessage {
    BASE_MESSAGE("다시 만나서 반가워요! 냉장고 상황을 업데이트해주세요 🔄"),

    EXPIRED_ITEMS("⚠️ 만료된 식재료 %d개를 정리해주세요!"),
    URGENT_ITEMS("🔥 긴급! 1일 내 만료 예정 식재료 %d개!"),
    EXPIRING_ITEMS("⏰ 2-3일 내 만료 예정 식재료 %d개가 있어요!"),
    NORMAL_STATE("✨ 냉장고가 잘 관리되고 있네요! 새로운 변화가 있다면 알려주세요");

    private final String template;

    SyncPromptMessage(String template) {
        this.template = template;
    }

    public String format(Object... args) {
        return String.format(template, args);
    }
}
