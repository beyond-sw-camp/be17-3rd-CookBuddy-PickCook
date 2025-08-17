package org.example.be17pickcook.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.domain.user.mapper.UserMapper;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.example.be17pickcook.domain.user.service.UserService;
import org.example.be17pickcook.template.EmailTemplates;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final EmailTemplates emailTemplates;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // 🔧 개선: 비밀번호 검증 로직 상수화
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String PASSWORD_PATTERN = ".*[a-zA-Z].*.*\\d.*.*[!@#$%^&*()].*";

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자 계정을 생성하고 이메일 인증을 위한 메일을 발송합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원가입 성공"),
                    @ApiResponse(responseCode = "400", description = "중복된 이메일 또는 잘못된 비밀번호"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<Void>> signup(@RequestBody UserDto.Register dto) {
        try {
            // 🔧 개선: 비밀번호 검증 메서드 분리
            validatePassword(dto.getPassword());

            userService.signup(dto);
            return ResponseEntity.ok(BaseResponse.success(null, BaseResponseStatus.SIGNUP_SUCCESS));
        } catch (IllegalArgumentException e) {
            // 🔧 개선: 구체적인 에러 메시지에 따른 상태 코드 반환
            BaseResponseStatus status = determineErrorStatus(e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(status));
        } catch (MessagingException e) {
            log.error("이메일 발송 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error(BaseResponseStatus.SERVER_ERROR));
        }
    }

    @Operation(
            summary = "이메일 인증",
            description = "회원가입 시 발송된 이메일의 인증 링크를 통해 계정을 활성화합니다."
    )
    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String uuid) {
        try {
            userService.verify(uuid);
            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(emailTemplates.getEmailVerificationCompletePage());
        } catch (Exception e) {
            String errorHtml = generateErrorHtml("인증 실패", e.getMessage(), "/signup");
            return ResponseEntity.badRequest()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(errorHtml);
        }
    }

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

        // 🔧 개선: 데이터베이스에서 최신 데이터 조회 (캐시 방지)
        User userFromDB = userRepository.findById(authUser.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UserDto.Response userResponse = userMapper.entityToResponse(userFromDB);
        return ResponseEntity.ok(BaseResponse.success(userResponse));
    }

    @Operation(
            summary = "사용자 정보 수정",
            description = "현재 로그인된 사용자의 프로필 정보를 수정합니다."
    )
    @PatchMapping("/profile")
    public ResponseEntity<BaseResponse<UserDto.Response>> updateProfile(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @RequestBody UserDto.UpdateProfile dto) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        try {
            UserDto.Response updatedUser = userService.updateProfile(authUser.getIdx(), dto);
            return ResponseEntity.ok(BaseResponse.success(updatedUser, BaseResponseStatus.PROFILE_UPDATE_SUCCESS));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, e.getMessage()));
        } catch (Exception e) {
            log.error("프로필 수정 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error(BaseResponseStatus.SERVER_ERROR));
        }
    }

    @Operation(
            summary = "사용자 로그아웃",
            description = "현재 로그인된 사용자를 로그아웃 처리하고 JWT 쿠키를 삭제합니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        // 🔧 개선: 로그아웃 로직을 서비스로 분리
        userService.logout(request, response);

        return ResponseEntity.ok(BaseResponse.success(null, BaseResponseStatus.LOGOUT_SUCCESS));
    }

    @Operation(
            summary = "아이디 찾기",
            description = "이름과 전화번호로 가입된 이메일 주소를 찾습니다."
    )
    @PostMapping("/find-email")
    public ResponseEntity<BaseResponse<UserDto.FindEmailResponse>> findEmail(
            @RequestBody UserDto.FindEmailRequest dto) {
        try {
            UserDto.FindEmailResponse result = userService.findEmailByNameAndPhone(dto);
            return ResponseEntity.ok(BaseResponse.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(BaseResponse.error(BaseResponseStatus.USER_NOT_FOUND));
        } catch (Exception e) {
            log.error("아이디 찾기 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error(BaseResponseStatus.SERVER_ERROR));
        }
    }

    @Operation(
            summary = "비밀번호 재설정 요청",
            description = "이메일로 비밀번호 재설정 링크를 발송합니다."
    )
    @PostMapping("/request-password-reset")
    public ResponseEntity<BaseResponse<Void>> requestPasswordReset(
            @RequestBody UserDto.PasswordResetRequest dto) {
        try {
            userService.requestPasswordReset(dto.getEmail());
            return ResponseEntity.ok(BaseResponse.success(null, "비밀번호 재설정 이메일이 발송되었습니다."));
        } catch (MessagingException e) {
            log.error("이메일 발송 실패", e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error(BaseResponseStatus.SERVER_ERROR));
        } catch (Exception e) {
            log.error("비밀번호 재설정 요청 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error(BaseResponseStatus.SERVER_ERROR));
        }
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
    public ResponseEntity<BaseResponse<Void>> resetPassword(@RequestBody UserDto.ResetPassword dto) {
        try {
            // 🔧 개선: 검증 로직 통합
            validatePasswordReset(dto);

            userService.resetPassword(dto.getToken(), dto.getNewPassword());
            return ResponseEntity.ok(BaseResponse.success(null, "비밀번호가 성공적으로 변경되었습니다."));

        } catch (IllegalArgumentException e) {
            BaseResponseStatus errorStatus = determinePasswordResetError(e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(errorStatus));
        } catch (Exception e) {
            log.error("비밀번호 재설정 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error(BaseResponseStatus.SERVER_ERROR));
        }
    }

    @Operation(
            summary = "이메일 중복 확인",
            description = "회원가입 시 입력한 이메일이 이미 사용 중인지 확인합니다."
    )
    @GetMapping("/check-email")
    public ResponseEntity<BaseResponse<Map<String, Object>>> checkEmailDuplicate(
            @RequestParam String email) {
        boolean exists = userRepository.findByEmailAndNotDeleted(email).isPresent();

        Map<String, Object> data = Map.of("available", !exists);
        BaseResponseStatus status = exists ? BaseResponseStatus.EMAIL_NOT_AVAILABLE : BaseResponseStatus.EMAIL_AVAILABLE;

        return ResponseEntity.ok(BaseResponse.success(data, status));
    }

    @Operation(
            summary = "닉네임 중복 확인",
            description = "회원정보 수정 시 입력한 닉네임이 이미 사용 중인지 확인합니다."
    )
    @GetMapping("/check-nickname")
    public ResponseEntity<BaseResponse<Map<String, Object>>> checkNicknameDuplicate(
            @Parameter(description = "확인할 닉네임", required = true) @RequestParam String nickname,
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        // 🔧 개선: 중복 체크 로직을 서비스로 분리
        Integer currentUserId = (authUser != null) ? authUser.getIdx() : null;
        boolean available = userService.isNicknameAvailable(nickname, currentUserId);

        Map<String, Object> data = Map.of("available", available);
        BaseResponseStatus status = available ? BaseResponseStatus.NICKNAME_AVAILABLE : BaseResponseStatus.NICKNAME_NOT_AVAILABLE;

        return ResponseEntity.ok(BaseResponse.success(data, status));
    }

    @Operation(
            summary = "회원탈퇴",
            description = "현재 로그인된 사용자의 계정을 탈퇴 처리합니다."
    )
    @PostMapping("/withdraw")
    public ResponseEntity<BaseResponse<UserDto.WithdrawResponse>> withdrawUser(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @RequestBody UserDto.WithdrawRequest dto,
            HttpServletResponse response) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        try {
            UserDto.WithdrawResponse result = userService.withdrawUser(authUser.getIdx(), dto);

            // 🔧 개선: 탈퇴 후 쿠키 삭제도 서비스에서 처리
            userService.clearAuthenticationCookies(response);

            return ResponseEntity.ok(BaseResponse.success(result, BaseResponseStatus.WITHDRAW_SUCCESS));
        } catch (IllegalArgumentException e) {
            BaseResponseStatus errorStatus = determineWithdrawError(e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(errorStatus));
        } catch (Exception e) {
            log.error("회원탈퇴 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error(BaseResponseStatus.SERVER_ERROR));
        }
    }

    // 🔧 개선: 헬퍼 메서드들을 private으로 분리

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }

        if (!password.matches(".*[a-zA-Z].*") ||
                !password.matches(".*\\d.*") ||
                !password.matches(".*[!@#$%^&*()].*")) {
            throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.");
        }
    }

    private void validatePasswordReset(UserDto.ResetPassword dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        validatePassword(dto.getNewPassword());
    }

    private BaseResponseStatus determineErrorStatus(String message) {
        if (message.contains("이미 사용")) {
            return BaseResponseStatus.DUPLICATE_EMAIL;
        }
        if (message.contains("비밀번호")) {
            return BaseResponseStatus.INVALID_PASSWORD_FORMAT;
        }
        return BaseResponseStatus.REQUEST_ERROR;
    }

    private BaseResponseStatus determinePasswordResetError(String message) {
        if (message.contains("일치하지")) return BaseResponseStatus.PASSWORD_MISMATCH;
        if (message.contains("비밀번호")) return BaseResponseStatus.INVALID_PASSWORD_FORMAT;
        if (message.contains("토큰")) return BaseResponseStatus.INVALID_TOKEN;
        return BaseResponseStatus.REQUEST_ERROR;
    }

    private BaseResponseStatus determineWithdrawError(String message) {
        if (message.contains("탈퇴 확인")) return BaseResponseStatus.WITHDRAW_CONFIRM_REQUIRED;
        if (message.contains("이미 탈퇴")) return BaseResponseStatus.ALREADY_WITHDRAWN;
        if (message.contains("비밀번호")) return BaseResponseStatus.INVALID_USER_INFO;
        return BaseResponseStatus.REQUEST_ERROR;
    }

    private String generateErrorHtml(String title, String message, String returnUrl) {
        return String.format("""
            <h2>%s: %s</h2>
            <a href='http://localhost:5173%s'>%s</a>
            """, title, message, returnUrl, "다시 시도하기");
    }
}