package org.example.be17pickcook.common;

import lombok.Getter;

/**
 * PickCook 프로젝트 통일 API 응답 클래스
 * - 모든 API 응답을 일관된 형식으로 제공
 * - 성공/실패 여부, 상태 코드, 메시지, 실제 데이터를 포함
 * - 불변 객체로 설계하여 안전성 확보
 */
@Getter
public class BaseResponse<T> {

    // =================================================================
    // 응답 필드
    // =================================================================

    private final boolean success;
    private final int code;
    private final String message;
    private final T results;

    // =================================================================
    // 생성자
    // =================================================================

    /**
     * BaseResponse 생성자
     * @param success 성공 여부
     * @param code 상태 코드
     * @param message 응답 메시지
     * @param results 실제 데이터
     */
    public BaseResponse(boolean success, int code, String message, T results) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.results = results;
    }

    // =================================================================
    // 성공 응답 생성 메서드들
    // =================================================================

    /**
     * 기본 성공 응답 생성
     * @param results 응답 데이터
     * @return BaseResponse 객체
     */
    public static <T> BaseResponse<T> success(T results) {
        return success(results, BaseResponseStatus.SUCCESS);
    }

    /**
     * 커스텀 상태코드로 성공 응답 생성
     * @param results 응답 데이터
     * @param status 응답 상태
     * @return BaseResponse 객체
     */
    public static <T> BaseResponse<T> success(T results, BaseResponseStatus status) {
        return new BaseResponse<>(
                status.isSuccess(),
                status.getCode(),
                status.getMessage(),
                results
        );
    }

    /**
     * 커스텀 메시지로 성공 응답 생성
     * @param results 응답 데이터
     * @param customMessage 커스텀 메시지
     * @return BaseResponse 객체
     */
    public static <T> BaseResponse<T> success(T results, String customMessage) {
        return new BaseResponse<>(
                true,
                BaseResponseStatus.SUCCESS.getCode(),
                customMessage,
                results
        );
    }

    // =================================================================
    // 실패 응답 생성 메서드들
    // =================================================================

    /**
     * 기본 에러 응답 생성
     * @param status 에러 상태
     * @return BaseResponse 객체
     */
    public static <T> BaseResponse<T> error(BaseResponseStatus status) {
        return new BaseResponse<>(
                status.isSuccess(),
                status.getCode(),
                status.getMessage(),
                null
        );
    }

    /**
     * 커스텀 메시지로 에러 응답 생성
     * @param status 에러 상태
     * @param customMessage 커스텀 에러 메시지
     * @return BaseResponse 객체
     */
    public static <T> BaseResponse<T> error(BaseResponseStatus status, String customMessage) {
        return new BaseResponse<>(
                status.isSuccess(),
                status.getCode(),
                customMessage,
                null
        );
    }

    /**
     * 에러 데이터를 포함한 에러 응답 생성 (Validation 오류 등에 사용)
     * @param status 에러 상태
     * @param errorData 에러 상세 데이터 (필드별 오류 정보 등)
     * @return BaseResponse 객체
     */
    public static <T> BaseResponse<T> error(BaseResponseStatus status, T errorData) {
        return new BaseResponse<>(
                status.isSuccess(),
                status.getCode(),
                status.getMessage(),
                errorData
        );
    }
}