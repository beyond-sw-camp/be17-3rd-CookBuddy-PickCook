package org.example.be17pickcook.common;

import lombok.Getter;

/**
 * PickCook 프로젝트 응답 상태 코드 관리
 * - 20000번대: 성공 응답
 * - 30000번대: 클라이언트 요청 오류 (Validation, 비즈니스 로직 오류)
 * - 40000번대: 서버 응답 처리 오류
 * - 50000번대: 데이터베이스 오류
 * - 60000번대: 서버 내부 오류
 */
@Getter
public enum BaseResponseStatus {

    // =================================================================
    // 20000번대: 성공 응답
    // =================================================================

    // 기본 성공
    SUCCESS(true, 20000, "요청에 성공하였습니다."),

    // 인증 관련 성공
    LOGIN_SUCCESS(true, 20001, "로그인에 성공하였습니다."),
    LOGOUT_SUCCESS(true, 20002, "로그아웃이 완료되었습니다."),

    // 회원가입 관련 성공
    SIGNUP_SUCCESS(true, 20100, "회원가입이 완료되었습니다. 이메일을 확인해주세요."),
    EMAIL_VERIFY_SUCCESS(true, 20101, "이메일 인증이 완료되었습니다."),
    EMAIL_VERIFICATION_SUCCESS(true, 20101, "이메일 인증이 완료되었습니다."), // 별칭 추가
    EMAIL_AVAILABLE(true, 20102, "사용 가능한 이메일입니다."),
    NICKNAME_AVAILABLE(true, 20103, "사용 가능한 닉네임입니다."),
    EMAIL_FOUND_SUCCESS(true, 20104, "이메일을 찾았습니다."), // 🆕 새로 추가

    // 프로필 관리 성공
    PROFILE_UPDATE_SUCCESS(true, 20200, "회원정보가 성공적으로 수정되었습니다."),

    // 비밀번호 관리 성공
    PASSWORD_RESET_EMAIL_SENT(true, 20300, "비밀번호 재설정 이메일이 발송되었습니다."),
    PASSWORD_RESET_SUCCESS(true, 20301, "비밀번호가 성공적으로 변경되었습니다."),
    INTERNAL_TOKEN_GENERATED(true, 20302, "비밀번호 변경 토큰이 생성되었습니다."), // 🆕 새로 추가
    TOKEN_VALID(true, 20303, "유효한 토큰입니다."), // 🆕 새로 추가

    // OAuth2 관련 성공 (🆕 새로 추가)
    OAUTH_REDIRECT_REQUIRED(true, 20310, "소셜 로그인 사용자 리다이렉트"),
    OAUTH_PASSWORD_REDIRECT_SUCCESS(true, 20311, "카카오 계정 관리 페이지로 안내되었습니다."),

    // 회원탈퇴 성공
    WITHDRAW_SUCCESS(true, 20400, "회원탈퇴가 완료되었습니다."),

    // =================================================================
    // 30000번대: 클라이언트 요청 오류
    // =================================================================

    // 기본 요청 오류
    REQUEST_ERROR(false, 30000, "입력값을 확인해주세요."),

    // JWT 인증 오류
    EXPIRED_JWT(false, 30001, "JWT 토큰이 만료되었습니다."),
    INVALID_JWT(false, 30002, "유효하지 않은 JWT입니다."),
    INVALID_USER_ROLE(false, 30003, "권한이 없는 유저의 접근입니다."),
    UNAUTHORIZED(false, 30004, "인증이 필요합니다."),

    // 회원가입 관련 오류
    DUPLICATE_EMAIL(false, 30100, "이미 사용 중인 이메일입니다."),
    EMAIL_NOT_AVAILABLE(false, 30101, "이미 사용 중인 이메일입니다."),
    INVALID_EMAIL_TOKEN(false, 30102, "유효하지 않은 인증 코드입니다."),
    EXPIRED_EMAIL_TOKEN(false, 30103, "인증 코드가 만료되었습니다."),
    NICKNAME_NOT_AVAILABLE(false, 30104, "이미 사용 중인 닉네임입니다."),
    INVALID_NICKNAME_LENGTH(false, 30105, "닉네임은 2자 이상 20자 이하로 입력해주세요."),

    // 사용자 조회 오류
    USER_NOT_FOUND(false, 30200, "사용자를 찾을 수 없습니다."),
    INVALID_USER_INFO(false, 30201, "이메일 또는 비밀번호가 올바르지 않습니다."),

    // 비밀번호 재설정 오류
    INVALID_RESET_TOKEN(false, 30300, "유효하지 않은 재설정 토큰입니다."),
    EXPIRED_RESET_TOKEN(false, 30301, "재설정 토큰이 만료되었습니다."),
    INVALID_TOKEN(false, 30302, "유효하지 않은 토큰입니다."),
    SAME_AS_CURRENT_PASSWORD(false, 30303, "기존 비밀번호와 동일합니다. 다른 비밀번호를 사용해주세요."),
    PASSWORD_MISMATCH(false, 30304, "비밀번호가 일치하지 않습니다."),
    CURRENT_PASSWORD_REQUIRED(false, 30305, "현재 비밀번호를 입력해주세요."), // 🆕 새로 추가
    INVALID_CURRENT_PASSWORD(false, 30306, "현재 비밀번호가 일치하지 않습니다."), // 🆕 새로 추가
    TOKEN_ALREADY_USED(false, 30307, "이미 사용된 토큰입니다."), // 🆕 새로 추가

    // OAuth2 관련 오류 (🆕 새로 추가)
    NOT_OAUTH_USER(false, 30310, "일반 로그인 사용자입니다."),
    OAUTH_PROVIDER_NOT_SUPPORTED(false, 30311, "지원하지 않는 OAuth2 제공자입니다."),
    OAUTH_TOKEN_INVALID(false, 30312, "OAuth2 토큰이 유효하지 않습니다."),

    // 회원탈퇴 오류
    WITHDRAW_CONFIRM_REQUIRED(false, 30400, "탈퇴 확인이 필요합니다."),
    ALREADY_WITHDRAWN(false, 30401, "이미 탈퇴한 계정입니다."),
    WITHDRAWAL_NOT_ALLOWED(false, 30402, "탈퇴할 수 없는 상태입니다."),

    // =================================================================
    // 31000번대: 커뮤니티 기능 오류
    // =================================================================

    POST_NOT_FOUND(false, 31001, "존재하지 않는 게시글입니다."),

    // =================================================================
    // 40000번대: 서버 응답 처리 오류
    // =================================================================

    RESPONSE_ERROR(false, 40001, "값을 불러오는데 실패하였습니다."),
    RESPONSE_NULL_ERROR(false, 40002, "요청된 데이터를 찾을 수 없습니다."),

    // 결제 관련 오류
    ORDERS_VALIDATION_FAIL(false, 40100, "결제 정보가 잘못되었습니다."),
    IAMPORT_ERROR(false, 40101, "결제 금액이 잘못되었습니다."),
    ORDERS_NOT_ORDERED(false, 40102, "결제 정보가 없습니다. 구매 후 이용해주세요."),

    // =================================================================
    // 50000번대: 데이터베이스 오류
    // =================================================================

    DATABASE_ERROR(false, 50001, "데이터베이스 연결에 실패하였습니다."),

    // =================================================================
    // 60000번대: 서버 내부 오류
    // =================================================================

    SERVER_ERROR(false, 60001, "서버와의 연결에 실패하였습니다.");

    // =================================================================
    // 필드 및 생성자
    // =================================================================

    private final boolean isSuccess;
    private final int code;
    private final String message;

    BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}