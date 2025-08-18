package org.example.be17pickcook.common.exception;

import org.example.be17pickcook.common.BaseResponseStatus;
import lombok.Getter;

/**
 * 🎯 PickCook 프로젝트 커스텀 예외 클래스
 * - BaseResponseStatus와 연동하여 통일된 오류 처리
 * - 정적 팩토리 메서드로 간편한 예외 생성
 */
@Getter
public class BaseException extends RuntimeException {

    private final BaseResponseStatus status;

    // 🔧 생성자 1: 상태코드 + 커스텀 메시지
    public BaseException(BaseResponseStatus status, String message) {
        super(message);
        this.status = status;
    }

    // 🔧 생성자 2: 상태코드만 (기본 메시지 사용)
    public BaseException(BaseResponseStatus status) {
        super(status.getMessage());
        this.status = status;
    }

    // 🔧 정적 팩토리 메서드: 가장 많이 사용할 방식
    public static BaseException from(BaseResponseStatus status) {
        return new BaseException(status);
    }

    // 🔧 정적 팩토리 메서드: 커스텀 메시지가 필요한 경우
    public static BaseException of(BaseResponseStatus status, String customMessage) {
        return new BaseException(status, customMessage);
    }
}