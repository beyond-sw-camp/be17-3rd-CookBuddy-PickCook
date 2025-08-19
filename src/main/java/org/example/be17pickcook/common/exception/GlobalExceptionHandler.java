package org.example.be17pickcook.common.exception;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 *   PickCook 프로젝트 전역 예외 처리기
 * - 모든 Controller에서 발생하는 예외를 통합 처리
 * - BaseResponse 형식으로 일관된 응답 보장
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // PickCook 상태코드 → HTTP 상태코드 변환
    private int httpStatusCodeMapper(int statusCode) {
        if (statusCode >= 50000) {
            return 500; // 서버 오류 (DATABASE_ERROR, SERVER_ERROR)
        } else if (statusCode >= 40000) {
            return 500; // 응답 오류 (RESPONSE_ERROR, ORDERS_ERROR 등)
        } else if (statusCode >= 30000) {
            return 400; // 요청 오류 (REQUEST_ERROR, DUPLICATE_EMAIL 등)
        } else if (statusCode >= 20000) {
            return 200; // 성공 (하지만 일부는 401, 403으로 세분화 가능)
        }
        return 400; // 기본값
    }

    // 1. 커스텀 BaseException 처리 (가장 중요!)
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException e) {
        log.info("BaseException 발생: {} (코드: {})", e.getMessage(), e.getStatus().getCode());

        return ResponseEntity.status(httpStatusCodeMapper(e.getStatus().getCode()))
                .body(BaseResponse.error(e.getStatus()));
    }

    // 2. Validation 예외 처리 (@Valid 실패 시)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e) {

        // 모든 필드 오류를 Map으로 수집
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        log.info("Validation 오류: {}", errors);

        // 첫 번째 오류 메시지를 주 메시지로 사용
        String firstErrorMessage = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        return ResponseEntity.badRequest()
                .body(new BaseResponse<>(false,
                        BaseResponseStatus.REQUEST_ERROR.getCode(),
                        firstErrorMessage,
                        errors));
    }

    // 3. PickCook 특화: 이메일 발송 예외 처리
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<BaseResponse<Void>> handleMessagingException(MessagingException e) {
        log.error("이메일 발송 실패: {}", e.getMessage());

        return ResponseEntity.internalServerError()
                .body(BaseResponse.error(BaseResponseStatus.SERVER_ERROR));
    }

    // 4. PickCook 특화: 데이터베이스 제약조건 위반 처리
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleDataIntegrityViolationException(
            DataIntegrityViolationException e) {

        log.warn("데이터 무결성 위반: {}", e.getMessage());

        // 제약조건 종류별 세분화된 처리
        String message = e.getMessage();
        if (message != null) {
            if (message.contains("email")) {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.error(BaseResponseStatus.DUPLICATE_EMAIL));
            } else if (message.contains("nickname")) {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.error(BaseResponseStatus.NICKNAME_NOT_AVAILABLE));
            }
        }

        return ResponseEntity.badRequest()
                .body(BaseResponse.error(BaseResponseStatus.DATABASE_ERROR));
    }

    // 5. 일반적인 IllegalArgumentException 처리 (기존 코드 호환성)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException e) {

        log.info("잘못된 인자: {}", e.getMessage());

        return ResponseEntity.badRequest()
                .body(new BaseResponse<>(false,
                        BaseResponseStatus.REQUEST_ERROR.getCode(),
                        e.getMessage(),
                        null));
    }

    // 정적 리소스 오류 처리 (프론트엔드 JSON 파일 요청)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        String resourcePath = e.getResourcePath();

        // JSON 파일 요청은 debug 레벨로만 로깅 (로그 스팸 방지)
        if (resourcePath != null && resourcePath.endsWith(".json")) {
            log.debug("정적 JSON 리소스를 찾을 수 없음: {}", resourcePath);
        } else {
            // 다른 정적 리소스 오류는 일반 로그로 기록
            log.warn("정적 리소스를 찾을 수 없음: {}", resourcePath);
        }

        return ResponseEntity.status(404)
                .body(BaseResponse.error(BaseResponseStatus.RESPONSE_ERROR, "요청한 리소스를 찾을 수 없습니다."));
    }

    // 6. 예상하지 못한 모든 예외 처리 (안전망)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGenericException(Exception e) {
        log.error("예상하지 못한 오류 발생", e);

        return ResponseEntity.internalServerError()
                .body(BaseResponse.error(BaseResponseStatus.SERVER_ERROR));
    }
}