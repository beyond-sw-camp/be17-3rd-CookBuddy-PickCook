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
    // 배송지 관련 성공 (20500번대)
    // =================================================================

    // 배송지 CRUD 성공
    ADDRESS_CREATE_SUCCESS(true, 20500, "배송지가 성공적으로 추가되었습니다."),
    ADDRESS_UPDATE_SUCCESS(true, 20501, "배송지가 성공적으로 수정되었습니다."),
    ADDRESS_DELETE_SUCCESS(true, 20502, "배송지가 성공적으로 삭제되었습니다."),
    ADDRESS_LIST_SUCCESS(true, 20503, "배송지 목록 조회가 완료되었습니다."),

    // 배송지 기본 설정 성공
    ADDRESS_DEFAULT_SET_SUCCESS(true, 20510, "기본배송지가 설정되었습니다."),
    ADDRESS_DEFAULT_AUTO_SET_SUCCESS(true, 20511, "첫 번째 배송지가 기본배송지로 자동 설정되었습니다."),

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
    EMAIL_NOT_AVAILABLE(false, 30101, "이미 사용 중인 이메일입니다."),
    INVALID_EMAIL_TOKEN(false, 30102, "유효하지 않은 인증 코드입니다."),
    EXPIRED_EMAIL_TOKEN(false, 30103, "인증 코드가 만료되었습니다."),
    NICKNAME_NOT_AVAILABLE(false, 30104, "이미 사용 중인 닉네임입니다."),
    INVALID_NICKNAME_LENGTH(false, 30105, "닉네임은 2자 이상 20자 이하로 입력해주세요."),

    // 사용자 조회 오류
    USER_NOT_FOUND(false, 30200, "사용자를 찾을 수 없습니다."),
    INVALID_USER_INFO(false, 30201, "이메일 또는 비밀번호가 올바르지 않습니다."),
    PHONE_DUPLICATE_FOUND(false, 30205, "동일한 전화번호로 가입된 계정이 여러 개 있습니다. 고객센터에 문의해주세요."),

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

    // 냉장고 아이템 관련 오류
    REFRIGERATOR_ITEM_NOT_FOUND(false, 30500, "냉장고 아이템을 찾을 수 없습니다."),
    REFRIGERATOR_ITEM_ACCESS_DENIED(false, 30501, "해당 냉장고 아이템에 접근할 권한이 없습니다."),
    REFRIGERATOR_ITEM_ALREADY_DELETED(false, 30502, "이미 삭제된 냉장고 아이템입니다."),
    REFRIGERATOR_ITEM_CANNOT_RESTORE(false, 30503, "복원할 수 없는 냉장고 아이템입니다."),

    // 카테고리 관련 오류
    CATEGORY_NOT_FOUND(false, 30600, "카테고리를 찾을 수 없습니다."),
    CATEGORY_NAME_DUPLICATE(false, 30601, "이미 존재하는 카테고리명입니다."),
    CATEGORY_IN_USE_CANNOT_DELETE(false, 30602, "사용 중인 카테고리는 삭제할 수 없습니다."),

    // =================================================================
    // 배송지 관련 오류 (30700번대) ✅ 수정됨
    // =================================================================

    // 배송지 기본 오류
    ADDRESS_NOT_FOUND(false, 30700, "배송지를 찾을 수 없습니다."),
    ADDRESS_ACCESS_DENIED(false, 30701, "해당 배송지에 접근할 권한이 없습니다."),

    // 배송지 유효성 검증 오류
    ADDRESS_DUPLICATE_ERROR(false, 30710, "동일한 주소가 이미 등록되어 있습니다."),
    ADDRESS_POSTAL_CODE_INVALID(false, 30711, "올바르지 않은 우편번호 형식입니다."),
    ADDRESS_ROAD_ADDRESS_REQUIRED(false, 30712, "도로명주소는 필수입니다."),
    ADDRESS_DETAIL_ADDRESS_REQUIRED(false, 30713, "상세주소는 필수입니다."),

    // 기본배송지 관리 오류
    ADDRESS_DEFAULT_REQUIRED(false, 30720, "최소 하나의 기본배송지가 필요합니다."),
    ADDRESS_DEFAULT_ALREADY_EXISTS(false, 30721, "이미 기본배송지가 설정되어 있습니다."),
    ADDRESS_DEFAULT_NOT_FOUND(false, 30722, "기본배송지를 찾을 수 없습니다."),

    // 배송지 개수 제한 오류 (선택사항)
    ADDRESS_LIMIT_EXCEEDED(false, 30730, "배송지는 최대 10개까지 등록 가능합니다."),

    // =================================================================
    // 31000번대: 커뮤니티 기능 오류
    // =================================================================

    POST_NOT_FOUND(false, 31001, "존재하지 않는 게시글입니다."),

    // =================================================================
    // 40000번대: 서버 응답 처리 오류
    // =================================================================

    RESPONSE_ERROR(false, 40001, "값을 불러오는데 실패하였습니다."),
    RESPONSE_NULL_ERROR(false, 40002, "요청된 데이터를 찾을 수 없습니다."),
    RESOURCE_NOT_FOUND(false, 40003, "요청한 리소스를 찾을 수 없습니다."), // 🆕 추가

    // 결제 관련 오류
    ORDERS_VALIDATION_FAIL(false, 40100, "결제 정보가 잘못되었습니다."),
    IAMPORT_ERROR(false, 40101, "결제 금액이 잘못되었습니다."),
    ORDERS_NOT_ORDERED(false, 40102, "결제 정보가 없습니다. 구매 후 이용해주세요."),

    // 필터링 관련 에러
    INVALID_FILTER_CONDITION(false, 4901, "유효하지 않은 필터 조건입니다."),
    INVALID_SORT_TYPE(false, 4902, "유효하지 않은 정렬 기준입니다."),
    INVALID_EXPIRATION_STATUS(false, 4903, "유효하지 않은 유통기한 상태입니다."),
    FILTER_PROCESSING_ERROR(false, 4904, "필터링 처리 중 오류가 발생했습니다."),

    // =================================================================
    // 41000번대: 리뷰 관련 오류
    // =================================================================

    // 리뷰 기본 오류 (41000-41099)
    REVIEW_NOT_FOUND(false, 41000, "요청한 리뷰를 찾을 수 없습니다."),
    REVIEW_ACCESS_DENIED(false, 41001, "해당 리뷰에 대한 접근 권한이 없습니다."),
    REVIEW_ALREADY_DELETED(false, 41002, "이미 삭제된 리뷰입니다."),
    REVIEW_CREATION_FAILED(false, 41003, "리뷰 작성에 실패했습니다."),
    REVIEW_UPDATE_FAILED(false, 41004, "리뷰 수정에 실패했습니다."),
    REVIEW_DELETE_FAILED(false, 41005, "리뷰 삭제에 실패했습니다."),

    // 리뷰 권한 관련 (41100-41199)
    REVIEW_NO_PURCHASE_HISTORY(false, 41100, "해당 상품을 구매한 이력이 없어 리뷰를 작성할 수 없습니다."),
    REVIEW_DUPLICATE_NOT_ALLOWED(false, 41101, "이미 해당 상품에 리뷰를 작성하셨습니다."),
    REVIEW_AUTHOR_MISMATCH(false, 41102, "본인이 작성한 리뷰만 수정/삭제할 수 있습니다."),
    REVIEW_MODIFICATION_PERIOD_EXPIRED(false, 41103, "리뷰 수정 기간이 만료되었습니다. (작성 후 7일 이내만 수정 가능)"),
    REVIEW_PURCHASE_DATE_INVALID(false, 41104, "구매일로부터 6개월이 지나 리뷰를 작성할 수 없습니다."),

    // 리뷰 내용 검증 관련 (41200-41299)
    REVIEW_TITLE_TOO_LONG(false, 41200, "리뷰 제목은 100자 이하로 작성해주세요."),
    REVIEW_TITLE_REQUIRED(false, 41201, "리뷰 제목을 입력해주세요."),
    REVIEW_CONTENT_TOO_LONG(false, 41202, "리뷰 내용은 2000자 이하로 작성해주세요."),
    REVIEW_CONTENT_REQUIRED(false, 41203, "리뷰 내용을 입력해주세요."),
    REVIEW_RATING_INVALID(false, 41204, "별점은 1점부터 5점까지 선택할 수 있습니다."),
    REVIEW_RATING_REQUIRED(false, 41205, "별점을 선택해주세요."),

    // 리뷰 이미지 관련 (41300-41399)
    REVIEW_IMAGE_NOT_FOUND(false, 41300, "요청한 리뷰 이미지를 찾을 수 없습니다."),
    REVIEW_IMAGE_COUNT_EXCEEDED(false, 41301, "리뷰 이미지는 최대 5개까지 업로드할 수 있습니다."),
    REVIEW_IMAGE_SIZE_EXCEEDED(false, 41302, "이미지 파일 크기는 10MB 이하여야 합니다."),
    REVIEW_IMAGE_FORMAT_UNSUPPORTED(false, 41303, "지원하지 않는 이미지 형식입니다. (JPEG, PNG, WebP만 가능)"),
    REVIEW_IMAGE_UPLOAD_FAILED(false, 41304, "이미지 업로드에 실패했습니다. 다시 시도해주세요."),
    REVIEW_IMAGE_DELETE_FAILED(false, 41305, "이미지 삭제에 실패했습니다."),
    REVIEW_IMAGE_URL_INVALID(false, 41306, "올바르지 않은 이미지 URL입니다."),
    REVIEW_IMAGE_ORDER_INVALID(false, 41307, "이미지 순서는 1부터 5까지 설정할 수 있습니다."),

    // =================================================================
    // 40200번대: 상품-레시피 연관 기능 오류
    // =================================================================

    INVALID_RECIPE_ID(false, 40200, "유효하지 않은 레시피 ID입니다."),
    RECIPE_NOT_FOUND(false, 40201, "존재하지 않는 레시피입니다."),
    RECIPE_INGREDIENTS_NOT_FOUND(false, 40202, "레시피 재료 정보를 찾을 수 없습니다."),
    RELATED_PRODUCTS_QUERY_FAILED(false, 40203, "연관 상품 조회 중 오류가 발생했습니다."),
    INSUFFICIENT_PRODUCTS_FOR_RECOMMENDATION(false, 40204, "추천할 수 있는 상품이 부족합니다."),

    // =================================================================
    // 50000번대: 데이터베이스 오류
    // =================================================================
    DATABASE_ERROR(false, 50000, "데이터베이스 오류가 발생했습니다."),
    CONNECTION_ERROR(false, 50001, "데이터베이스 연결에 실패하였습니다."),

    // =================================================================
    // 60000번대: 서버 내부 오류
    // =================================================================
    EMAIL_SEND_FAILED(false, 60000, "인증 이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요."),
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