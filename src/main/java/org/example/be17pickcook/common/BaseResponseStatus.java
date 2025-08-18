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
    // 성공 응답
    SUCCESS(true, 20000, "요청에 성공하였습니다."),
    LOGIN_SUCCESS(true, 20101, "로그인에 성공하였습니다."),
    LOGOUT_SUCCESS(true, 20102, "로그아웃이 완료되었습니다."), // 추가
    SIGNUP_SUCCESS(true, 20100, "회원가입이 완료되었습니다. 이메일을 확인해주세요."),
    EMAIL_VERIFY_SUCCESS(true, 20102, "이메일 인증이 완료되었습니다."),
    EMAIL_AVAILABLE(true, 20103, "사용 가능한 이메일입니다."),

    // 추가: 닉네임 관련 성공 응답
    NICKNAME_AVAILABLE(true, 20104, "사용 가능한 닉네임입니다."),
    PROFILE_UPDATE_SUCCESS(true, 20105, "회원정보가 성공적으로 수정되었습니다."),

    // 🔧 추가: 비밀번호 재설정 관련 성공 응답
    PASSWORD_RESET_EMAIL_SENT(true, 20106, "비밀번호 재설정 이메일이 발송되었습니다."),
    PASSWORD_RESET_SUCCESS(true, 20107, "비밀번호가 성공적으로 변경되었습니다."),

    // 🔧 추가: 회원탈퇴 관련 상태 코드
    WITHDRAW_SUCCESS(true, 20108, "회원탈퇴가 완료되었습니다."),

    /**
     * 30000 : Request 오류, Validation 오류
     */
    // 요청 오류
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


    // 추가: 닉네임 관련 오류
    NICKNAME_NOT_AVAILABLE(false, 30107, "이미 사용 중인 닉네임입니다."),
    INVALID_NICKNAME_LENGTH(false, 30108, "닉네임은 2자 이상 20자 이하로 입력해주세요."),

    // 🔧 추가: 비밀번호 재설정 관련 오류
    INVALID_RESET_TOKEN(false, 30109, "유효하지 않은 재설정 토큰입니다."),
    EXPIRED_RESET_TOKEN(false, 30110, "재설정 토큰이 만료되었습니다."),
    INVALID_TOKEN(false, 30117, "유효하지 않은 토큰입니다."),
    SAME_AS_CURRENT_PASSWORD(false, 30111, "기존 비밀번호와 동일합니다. 다른 비밀번호를 사용해주세요."),
    INVALID_PASSWORD_FORMAT(false, 30112, "비밀번호는 8자 이상, 영문+숫자+특수문자를 포함해야 합니다."),
    PASSWORD_MISMATCH(false, 30113, "비밀번호가 일치하지 않습니다."),

    // 회원탈퇴 관련 오류
    WITHDRAW_CONFIRM_REQUIRED(false, 30114, "탈퇴 확인이 필요합니다."),
    ALREADY_WITHDRAWN(false, 30115, "이미 탈퇴한 계정입니다."),
    WITHDRAWAL_NOT_ALLOWED(false, 30116, "탈퇴할 수 없는 상태입니다."),


    /**
     * 31000 : 커뮤니티 오류
     */
    POST_NOT_FOUND(false, 31001, "존재하지 않는 게시글입니다."),

    /**
     * 40000 : Response 오류
     */
    // 응답 오류
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