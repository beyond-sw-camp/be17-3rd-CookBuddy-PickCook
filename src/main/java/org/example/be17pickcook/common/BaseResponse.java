package org.example.be17pickcook.common;

import lombok.Getter;
import lombok.Setter;

/**
 * 🔧 개선된 통일 API 응답 클래스
 */
@Getter
@Setter
public class BaseResponse<T> {
    private boolean success;
    private int code;
    private String message;
    private T results;

    // 🔧 기본 생성자
    public BaseResponse(boolean success, int code, String message, T results) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.results = results;
    }

    // 🔧 개선: 성공 응답 생성 메서드들
    public static <T> BaseResponse<T> success(T results) {
        return success(results, BaseResponseStatus.SUCCESS);
    }

    public static <T> BaseResponse<T> success(T results, BaseResponseStatus status) {
        return new BaseResponse<>(
                status.isSuccess(),
                status.getCode(),
                status.getMessage(),
                results
        );
    }

    public static <T> BaseResponse<T> success(T results, String customMessage) {
        return new BaseResponse<>(
                true,
                BaseResponseStatus.SUCCESS.getCode(),
                customMessage,
                results
        );
    }

    // 🔧 개선: 에러 응답 생성 메서드들
    public static <T> BaseResponse<T> error(BaseResponseStatus status) {
        return new BaseResponse<>(
                status.isSuccess(),
                status.getCode(),
                status.getMessage(),
                null
        );
    }

    public static <T> BaseResponse<T> error(BaseResponseStatus status, String customMessage) {
        return new BaseResponse<>(
                status.isSuccess(),
                status.getCode(),
                customMessage,
                null
        );
    }

    public static <T> BaseResponse<T> error(BaseResponseStatus status, T data) {
        return new BaseResponse<>(
                status.isSuccess(),
                status.getCode(),
                status.getMessage(),
                data
        );
    }

    // 🔧 개선: 빌더 패턴 지원
    public static <T> ResponseBuilder<T> builder() {
        return new ResponseBuilder<>();
    }

    public static class ResponseBuilder<T> {
        private boolean success = true;
        private int code = BaseResponseStatus.SUCCESS.getCode();
        private String message = BaseResponseStatus.SUCCESS.getMessage();
        private T results;

        public ResponseBuilder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public ResponseBuilder<T> code(int code) {
            this.code = code;
            return this;
        }

        public ResponseBuilder<T> message(String message) {
            this.message = message;
            return this;
        }

        public ResponseBuilder<T> results(T results) {
            this.results = results;
            return this;
        }

        public ResponseBuilder<T> status(BaseResponseStatus status) {
            this.success = status.isSuccess();
            this.code = status.getCode();
            this.message = status.getMessage();
            return this;
        }

        public BaseResponse<T> build() {
            return new BaseResponse<>(success, code, message, results);
        }
    }
}