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
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.user.service.UserService;
import org.example.be17pickcook.template.EmailTemplates;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PickCook 사용자 관리 컨트롤러
 * - 회원가입, 인증, 프로필 관리, 비밀번호 재설정, 회원탈퇴 등 사용자 관련 API 제공
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
    // 의존성 주입
    // =================================================================

    private final UserService userService;
    private final EmailTemplates emailTemplates;

    // =================================================================
    // 회원가입 관련 API
    // =================================================================

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자 계정을 생성하고 이메일 인증을 위한 메일을 발송합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원가입 성공"),
                    @ApiResponse(responseCode = "400", description = "중복된 이메일 또는 잘못된 입력"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<Void>> signup(
            @Valid @RequestBody UserDto.Register dto,
            BindingResult bindingResult) throws Exception {

        // Validation 오류 처리
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        // GlobalExceptionHandler가 모든 예외 처리
        userService.signup(dto);
        return ResponseEntity.ok(BaseResponse.success(null, BaseResponseStatus.SIGNUP_SUCCESS));
    }

    @Operation(
            summary = "이메일 인증",
            description = "회원가입 시 발송된 이메일의 인증 링크를 통해 계정을 활성화합니다."
    )
    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String uuid) {
        // GlobalExceptionHandler가 예외 처리
        userService.verify(uuid);
        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(emailTemplates.getEmailVerificationCompletePage());
    }

    @GetMapping("/check-email")
    public ResponseEntity<BaseResponse<Map<String, Object>>> checkEmailDuplicate(
            @RequestParam String email) {

        // Service로 위임
        Map<String, Object> data = userService.checkEmailAvailability(email);
        boolean available = (Boolean) data.get("available");

        BaseResponseStatus status = available ?
                BaseResponseStatus.EMAIL_AVAILABLE : BaseResponseStatus.EMAIL_NOT_AVAILABLE;

        return ResponseEntity.ok(BaseResponse.success(data, status));
    }

    // =================================================================
    // 인증 및 세션 관리 API
    // =================================================================

    @Operation(
            summary = "현재 사용자 정보 조회",
            description = "현재 로그인된 사용자의 정보를 조회합니다."
    )
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserDto.Response>> getCurrentUser(
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        // Service로 위임 - 데이터베이스에서 최신 데이터 조회 및 DTO 변환
        UserDto.Response userResponse = userService.getCurrentUserInfo(authUser.getIdx());
        return ResponseEntity.ok(BaseResponse.success(userResponse));
    }

    @Operation(
            summary = "사용자 로그아웃",
            description = "현재 로그인된 사용자를 로그아웃 처리하고 JWT 쿠키를 삭제합니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        // 로그아웃 로직을 서비스로 분리
        userService.logout(request, response);

        return ResponseEntity.ok(BaseResponse.success(null, BaseResponseStatus.LOGOUT_SUCCESS));
    }

    // =================================================================
    // 프로필 관리 API
    // =================================================================

    @Operation(
            summary = "사용자 정보 수정",
            description = "현재 로그인된 사용자의 프로필 정보를 수정합니다."
    )
    @PatchMapping("/profile")
    public ResponseEntity<BaseResponse<UserDto.Response>> updateProfile(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Valid @RequestBody UserDto.UpdateProfile dto,
            BindingResult bindingResult) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        // Validation 오류 처리
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        // GlobalExceptionHandler가 예외 처리
        UserDto.Response updatedUser = userService.updateProfile(authUser.getIdx(), dto);
        return ResponseEntity.ok(BaseResponse.success(updatedUser, BaseResponseStatus.PROFILE_UPDATE_SUCCESS));
    }

    @Operation(
            summary = "닉네임 중복 확인",
            description = "회원정보 수정 시 입력한 닉네임이 이미 사용 중인지 확인합니다."
    )
    @GetMapping("/check-nickname")
    public ResponseEntity<BaseResponse<Map<String, Object>>> checkNicknameDuplicate(
            @Parameter(description = "확인할 닉네임", required = true) @RequestParam String nickname,
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        // 중복 체크 로직을 서비스로 분리
        Integer currentUserId = (authUser != null) ? authUser.getIdx() : null;
        boolean available = userService.isNicknameAvailable(nickname, currentUserId);

        Map<String, Object> data = Map.of("available", available);
        BaseResponseStatus status = available ? BaseResponseStatus.NICKNAME_AVAILABLE : BaseResponseStatus.NICKNAME_NOT_AVAILABLE;

        return ResponseEntity.ok(BaseResponse.success(data, status));
    }

    // =================================================================
    // 계정 찾기 API
    // =================================================================

    @Operation(
            summary = "아이디 찾기",
            description = "이름과 전화번호로 가입된 이메일 주소를 찾습니다."
    )
    @PostMapping("/find-email")
    public ResponseEntity<BaseResponse<UserDto.FindEmailResponse>> findEmail(
            @Valid @RequestBody UserDto.FindEmailRequest dto,
            BindingResult bindingResult) {

        // Validation 오류 처리
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        // GlobalExceptionHandler가 예외 처리
        UserDto.FindEmailResponse result = userService.findEmailByNameAndPhone(dto);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // =================================================================
    // 비밀번호 관리 API
    // =================================================================

    @Operation(
            summary = "비밀번호 재설정 요청",
            description = "이메일로 비밀번호 재설정 링크를 발송합니다."
    )
    @PostMapping("/request-password-reset")
    public ResponseEntity<BaseResponse<Void>> requestPasswordReset(
            @Valid @RequestBody UserDto.PasswordResetRequest dto,
            BindingResult bindingResult) throws Exception {

        // Validation 오류 처리
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        // GlobalExceptionHandler가 예외 처리
        userService.requestPasswordReset(dto.getEmail());
        return ResponseEntity.ok(BaseResponse.success(null, "비밀번호 재설정 이메일이 발송되었습니다."));
    }

    @Operation(
            summary = "비밀번호 재설정 페이지",
            description = "이메일 링크를 통해 접근하는 비밀번호 재설정 페이지입니다."
    )
    @GetMapping("/reset-password")
    public ResponseEntity<String> validateResetToken(@RequestParam String token) {
        boolean isValid = userService.validateResetToken(token);

        if (isValid) {
            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(emailTemplates.getPasswordResetPage(token));
        } else {
            return ResponseEntity.badRequest()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(emailTemplates.getPasswordResetErrorPage());
        }
    }

    @Operation(
            summary = "비밀번호 재설정",
            description = "새로운 비밀번호로 변경합니다."
    )
    @PostMapping("/reset-password")
    public ResponseEntity<BaseResponse<Void>> resetPassword(
            @Valid @RequestBody UserDto.ResetPassword dto,
            BindingResult bindingResult,
            HttpServletResponse response) {

        // Validation 오류 처리
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        // GlobalExceptionHandler가 예외 처리
        userService.resetPassword(dto.getToken(), dto.getNewPassword(), response);
        return ResponseEntity.ok(BaseResponse.success(null, "비밀번호가 성공적으로 변경되었습니다."));
    }

    @Operation(
            summary = "마이페이지 비밀번호 변경 토큰 생성",
            description = "현재 로그인된 사용자의 비밀번호 변경을 위한 토큰을 생성합니다. 이메일 발송 없이 즉시 토큰을 반환합니다."
    )
    @PostMapping("/generate-password-change-token")
    public ResponseEntity<BaseResponse<Map<String, String>>> generatePasswordChangeToken(
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        String token = userService.generatePasswordChangeToken(authUser.getIdx());
        Map<String, String> result = Map.of("token", token);

        return ResponseEntity.ok(BaseResponse.success(result, "비밀번호 변경 토큰이 생성되었습니다."));
    }

    // =================================================================
    // 회원탈퇴 API
    // =================================================================

    @Operation(
            summary = "회원탈퇴",
            description = "현재 로그인된 사용자의 계정을 탈퇴 처리합니다."
    )
    @PostMapping("/withdraw")
    public ResponseEntity<BaseResponse<UserDto.WithdrawResponse>> withdrawUser(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Valid @RequestBody UserDto.WithdrawRequest dto,
            BindingResult bindingResult,
            HttpServletResponse response) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        // Validation 오류 처리
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        // GlobalExceptionHandler가 예외 처리
        UserDto.WithdrawResponse result = userService.withdrawUser(authUser.getIdx(), dto);

        // 탈퇴 후 쿠키 삭제도 서비스에서 처리
        userService.clearAuthenticationCookies(response);

        return ResponseEntity.ok(BaseResponse.success(result, BaseResponseStatus.WITHDRAW_SUCCESS));
    }
}