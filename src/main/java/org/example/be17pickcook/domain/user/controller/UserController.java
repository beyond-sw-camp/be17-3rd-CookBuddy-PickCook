package org.example.be17pickcook.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.common.exception.BaseException;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.user.service.AuthService;
import org.example.be17pickcook.domain.user.service.UserService;
import org.example.be17pickcook.template.EmailTemplates;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PickCook 사용자 관리 컨트롤러 (리팩토링된 버전)
 * - 회원가입, 인증, 프로필 관리, 비밀번호 재설정, 회원탈퇴 등 사용자 관련 API 제공
 * - AuthService와 UserService를 활용한 책임 분리
 * - GlobalExceptionHandler를 통한 통일된 예외 처리
 * - @Valid 어노테이션을 활용한 자동 Validation
 */
@Tag(name = "사용자 관리", description = "회원가입, 로그인, 프로필 관리, 비밀번호 재설정 등 사용자 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    // =================================================================
    // 의존성 주입 (리팩토링됨)
    // =================================================================

    private final UserService userService;
    private final AuthService authService;
    private final EmailTemplates emailTemplates;

    // =================================================================
    // 회원가입 관련 API
    // =================================================================

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자 계정을 생성하고 이메일 인증을 위한 메일을 발송합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "회원가입 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일 또는 닉네임")
            }
    )
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<Void>> signup(
            @Parameter(description = "회원가입 정보", required = true)
            @Valid @RequestBody UserDto.Register dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(null, BaseResponseStatus.SIGNUP_SUCCESS));
    }

    @Operation(
            summary = "이메일 인증",
            description = "이메일 인증 토큰을 검증합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증 성공"),
                    @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰")
            }
    )
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(
            @Parameter(description = "이메일 인증 토큰", required = true, example = "uuid-token-example")
            @RequestParam String uuid) {
        try {
            userService.verify(uuid);
            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(emailTemplates.getEmailVerificationCompletePage());
        } catch (BaseException e) {
            return ResponseEntity.badRequest()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(emailTemplates.getEmailVerificationErrorPage(e.getMessage()));
        }
    }

    // =================================================================
    // 중복 확인 관련 API
    // =================================================================

    @Operation(
            summary = "이메일 중복 확인",
            description = "회원가입 시 입력한 이메일이 이미 사용 중인지 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "확인 완료")
            }
    )
    @GetMapping("/check-email")
    public ResponseEntity<BaseResponse<Map<String, Object>>> checkEmailDuplicate(
            @Parameter(description = "확인할 이메일", required = true, example = "user@example.com")
            @RequestParam String email) {

        Map<String, Object> data = userService.checkEmailAvailability(email);
        boolean available = (Boolean) data.get("available");
        BaseResponseStatus status = available ?
                BaseResponseStatus.EMAIL_AVAILABLE : BaseResponseStatus.EMAIL_NOT_AVAILABLE;

        return ResponseEntity.ok(BaseResponse.success(data, status));
    }

    @Operation(
            summary = "닉네임 중복 확인",
            description = "회원정보 수정 시 입력한 닉네임이 이미 사용 중인지 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "확인 완료")
            }
    )
    @GetMapping("/check-nickname")
    public ResponseEntity<BaseResponse<Map<String, Object>>> checkNicknameDuplicate(
            @Parameter(description = "확인할 닉네임", required = true, example = "새닉네임")
            @RequestParam String nickname,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        Integer currentUserId = (authUser != null) ? authUser.getIdx() : null;
        boolean available = userService.isNicknameAvailable(nickname, currentUserId);

        Map<String, Object> data = Map.of("available", available);
        BaseResponseStatus status = available ?
                BaseResponseStatus.NICKNAME_AVAILABLE : BaseResponseStatus.NICKNAME_NOT_AVAILABLE;

        return ResponseEntity.ok(BaseResponse.success(data, status));
    }

    // =================================================================
    // 인증 및 세션 관리 API
    // =================================================================

    @Operation(
            summary = "현재 사용자 정보 조회",
            description = "현재 로그인된 사용자의 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserDto.Response>> getCurrentUser(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        UserDto.Response userResponse = userService.getCurrentUserInfo(authUser.getIdx());
        return ResponseEntity.ok(BaseResponse.success(userResponse));
    }

    @Operation(
            summary = "로그아웃",
            description = "사용자 로그아웃을 처리합니다. 인증 쿠키가 삭제됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        userService.logout(request, response, authService);
        return ResponseEntity.ok(BaseResponse.success(null, BaseResponseStatus.LOGOUT_SUCCESS));
    }

    // =================================================================
    // 프로필 관리 API
    // =================================================================

    @Operation(
            summary = "사용자 정보 수정",
            description = "현재 로그인된 사용자의 프로필 정보를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "409", description = "중복된 닉네임")
            }
    )
    @PatchMapping("/profile")
    public ResponseEntity<BaseResponse<UserDto.Response>> updateProfile(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "수정할 프로필 정보", required = true)
            @Valid @RequestBody UserDto.UpdateProfile dto,
            BindingResult bindingResult) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        UserDto.Response updatedUser = userService.updateProfile(authUser.getIdx(), dto);
        return ResponseEntity.ok(BaseResponse.success(updatedUser, BaseResponseStatus.PROFILE_UPDATE_SUCCESS));
    }

    // =================================================================
    // 계정 찾기 API
    // =================================================================

    @Operation(
            summary = "아이디 찾기",
            description = "이름과 전화번호로 가입된 이메일 주소를 찾습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "아이디 찾기 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "404", description = "일치하는 사용자를 찾을 수 없음")
            }
    )
    @PostMapping("/find-email")
    public ResponseEntity<BaseResponse<UserDto.FindEmailResponse>> findEmail(
            @Parameter(description = "아이디 찾기 요청 정보", required = true)
            @Valid @RequestBody UserDto.FindEmailRequest dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        UserDto.FindEmailResponse result = userService.findEmailByNameAndPhone(dto);
        return ResponseEntity.ok(BaseResponse.success(result, BaseResponseStatus.EMAIL_FOUND_SUCCESS));
    }

    // =================================================================
    // 비밀번호 재설정 API (리팩토링됨)
    // =================================================================

    @Operation(
            summary = "비밀번호 재설정 이메일 발송",
            description = "비밀번호 재설정을 위한 이메일을 발송합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이메일 발송 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 이메일 형식"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 이메일")
            }
    )
    @PostMapping("/request-password-reset")
    public ResponseEntity<BaseResponse<Void>> requestPasswordReset(
            @Parameter(description = "비밀번호 재설정 이메일 요청", required = true)
            @Valid @RequestBody UserDto.PasswordResetEmailRequest dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        userService.requestPasswordReset(dto.getEmail());
        return ResponseEntity.ok(BaseResponse.success(null, BaseResponseStatus.PASSWORD_RESET_EMAIL_SENT));
    }

    @Operation(
            summary = "마이페이지용 비밀번호 변경 토큰 생성",
            description = "회원정보 관리에서 비밀번호 변경을 위한 내부 토큰을 생성합니다. (이메일 발송 없음)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 생성 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @PostMapping("/generate-password-change-token")
    public ResponseEntity<BaseResponse<Map<String, String>>> generatePasswordChangeToken(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        String token = userService.generatePasswordChangeToken(authUser.getIdx());
        Map<String, String> result = Map.of("token", token);

        return ResponseEntity.ok(BaseResponse.success(result, BaseResponseStatus.INTERNAL_TOKEN_GENERATED));
    }

    // =================================================================
    // 새로운 비밀번호 관리 API (완전 개선)
    // =================================================================

    @Operation(
            summary = "비밀번호 재설정 토큰 검증",
            description = "비밀번호 재설정 토큰의 유효성을 검증합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 검증 완료")
            }
    )
    @GetMapping("/validate-reset-token")
    public ResponseEntity<BaseResponse<Map<String, Boolean>>> validateResetToken(
            @Parameter(description = "검증할 토큰", required = true, example = "reset-token-example")
            @RequestParam String token) {

        boolean isValid = userService.validateResetToken(token);
        Map<String, Boolean> result = Map.of("valid", isValid);

        BaseResponseStatus status = isValid ?
                BaseResponseStatus.TOKEN_VALID : BaseResponseStatus.INVALID_TOKEN;

        return ResponseEntity.ok(BaseResponse.success(result, status));
    }

    @Operation(
            summary = "비밀번호 재설정",
            description = "토큰을 사용하여 새로운 비밀번호로 재설정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
            }
    )
    @PostMapping("/reset-password")
    public ResponseEntity<BaseResponse<Void>> resetPassword(
            @Parameter(description = "비밀번호 재설정 요청", required = true)
            @Valid @RequestBody UserDto.ResetPasswordRequest dto,
            BindingResult bindingResult,
            HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        userService.resetPassword(dto.getToken(), dto.getNewPassword(), response, authService);
        return ResponseEntity.ok(BaseResponse.success(null, BaseResponseStatus.PASSWORD_RESET_SUCCESS));
    }

    // =================================================================
    // OAuth2 사용자 처리 API (새로 추가)
    // =================================================================

    @Operation(
            summary = "소셜로그인 사용자 비밀번호 변경 안내",
            description = "카카오 로그인 사용자가 비밀번호 변경을 요청할 때 카카오 계정 관리 페이지로 리다이렉트합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "리다이렉트 정보 제공"),
                    @ApiResponse(responseCode = "400", description = "일반 사용자 (OAuth2가 아님)"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/oauth-password-redirect")
    public ResponseEntity<BaseResponse<Map<String, String>>> getOAuthPasswordRedirect(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        // OAuth2 사용자 확인 (이메일이 숫자로만 구성된 경우)
        boolean isOAuth2User = authUser.getEmail().matches("^\\d+$");

        if (isOAuth2User) {
            Map<String, String> result = Map.of(
                    "message", "소셜 로그인 사용자는 카카오 계정에서 비밀번호를 변경해주세요.",
                    "redirectUrl", "https://accounts.kakao.com/weblogin/account/info"
            );
            return ResponseEntity.ok(BaseResponse.success(result, BaseResponseStatus.OAUTH_REDIRECT_REQUIRED));
        } else {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.NOT_OAUTH_USER));
        }
    }

    // =================================================================
    // 회원탈퇴 API
    // =================================================================

    @Operation(
            summary = "회원탈퇴",
            description = "사용자 계정을 탈퇴 처리합니다. 탈퇴 후 인증 쿠키가 삭제됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원탈퇴 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @PostMapping("/withdraw")
    public ResponseEntity<BaseResponse<UserDto.WithdrawResponse>> withdrawUser(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "회원탈퇴 요청 정보", required = true)
            @Valid @RequestBody UserDto.WithdrawRequest dto,
            BindingResult bindingResult,
            HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        UserDto.WithdrawResponse result = userService.withdrawUser(authUser.getIdx(), dto);

        // 회원탈퇴 성공 시 인증 쿠키 삭제
        userService.clearAuthenticationCookies(response, authService);

        return ResponseEntity.ok(BaseResponse.success(result, BaseResponseStatus.WITHDRAW_SUCCESS));
    }
}