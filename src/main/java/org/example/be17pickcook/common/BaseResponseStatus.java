package org.example.be17pickcook.common;

import lombok.Getter;

/**
 * 에러 코드 관리
 */
@Getter
public enum BaseResponseStatus {
    /**
     * 20000 : 요청 성공
     */
    SUCCESS(true, 20000, "요청에 성공하였습니다."),
    LOGIN_SUCCESS(true, 20101, "로그인에 성공하였습니다."),
    LOGOUT_SUCCESS(true, 20102, "로그아웃이 완료되었습니다."), // 추가
    SIGNUP_SUCCESS(true, 20100, "회원가입이 완료되었습니다. 이메일을 확인해주세요."),
    EMAIL_VERIFY_SUCCESS(true, 20102, "이메일 인증이 완료되었습니다."),
    EMAIL_AVAILABLE(true, 20103, "사용 가능한 이메일입니다."),


    /**
     * 30000 : Request 오류, Validation 오류
     */
    // Common
    REQUEST_ERROR(false, 30001, "입력값을 확인해주세요."),
    EXPIRED_JWT(false, 20001, "JWT 토큰이 만료되었습니다."),
    INVALID_JWT(false, 20002, "유효하지 않은 JWT입니다."),
    INVALID_USER_ROLE(false,20003,"권한이 없는 유저의 접근입니다."),

    // User 관련 오류 응답 추가
    DUPLICATE_EMAIL(false, 30100, "이미 사용 중인 이메일입니다."),
    EMAIL_NOT_AVAILABLE(false, 30101, "이미 사용 중인 이메일입니다."),
    INVALID_EMAIL_TOKEN(false, 30102, "유효하지 않은 인증 코드입니다."),
    EXPIRED_EMAIL_TOKEN(false, 30103, "인증 코드가 만료되었습니다."),
    USER_NOT_FOUND(false, 30104, "사용자를 찾을 수 없습니다."),
    UNAUTHORIZED(false, 30105, "인증이 필요합니다."),
    INVALID_USER_INFO(false, 30106, "이메일 또는 비밀번호가 올바르지 않습니다."),

    /**
     * 31000 : 커뮤니티 오류
     */
    POST_NOT_FOUND(false, 31001, "존재하지 않는 게시글입니다."),



    /**
     * 40000 : Response 오류
     */
    // Common
    RESPONSE_ERROR(false, 40001, "값을 불러오는데 실패하였습니다."),
    RESPONSE_NULL_ERROR(false,40002,"[NULL]입력된 IDX 값로 접근한 DB의 유효한 ROW가 존재하지 않습니다."),

    ORDERS_VALIDATION_FAIL(false, 40003, "결제 정보가 잘못되었습니다."),
    IAMPORT_ERROR(false, 40004, "결제 금액이 잘못되었습니다."),
    ORDERS_NOT_ORDERED(false, 40005, "결제 정보가 없습니다. 구매 후 이용해주세요."),

    /**
     * 50000 : Database 오류
     */
    DATABASE_ERROR(false, 50001, "데이터베이스 연결에 실패하였습니다."),

    /**
     * 60000 : Server 오류
     */
    SERVER_ERROR(false, 60001, "서버와의 연결에 실패하였습니다.");



    /**
     * 70000 : 커스텀
     */



    // 70000 : 필요시 만들어서 쓰세요


    private final boolean isSuccess;
    private final int code;
    private final String message;

    BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}