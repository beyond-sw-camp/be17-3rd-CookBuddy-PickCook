package org.example.be17pickcook.domain.user.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.domain.user.mapper.UserMapper;
import org.example.be17pickcook.template.EmailTemplates;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.example.be17pickcook.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final EmailTemplates emailTemplates;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Operation(
            summary = "사용자 로그아웃",
            description = "현재 로그인된 사용자를 로그아웃 처리하고 JWT 쿠키를 삭제합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그아웃 성공",
                            content = @Content(schema = @Schema(implementation = BaseResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류",
                            content = @Content(schema = @Schema(implementation = BaseResponse.class))
                    )
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(HttpServletResponse response) {

        Cookie jwtCookie = new Cookie("PICKCOOK_AT", null);
        jwtCookie.setMaxAge(0);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(
                new BaseResponse<>(true, BaseResponseStatus.LOGOUT_SUCCESS.getCode(),
                        BaseResponseStatus.LOGOUT_SUCCESS.getMessage(), null)
        );
    }

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자 계정을 생성하고 이메일 인증을 위한 메일을 발송합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원가입 성공",
                            content = @Content(schema = @Schema(implementation = BaseResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "중복된 이메일",
                            content = @Content(schema = @Schema(implementation = BaseResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류",
                            content = @Content(schema = @Schema(implementation = BaseResponse.class))
                    )
            }
    )
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<Void>> signup(@RequestBody UserDto.Register dto) {
        try {
            userService.signup(dto);
            return ResponseEntity.ok(BaseResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.DUPLICATE_EMAIL));
        } catch (MessagingException e) {
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error(BaseResponseStatus.SERVER_ERROR));
        }
    }

    @Operation(
            summary = "이메일 인증",
            description = "회원가입 시 발송된 이메일의 인증 링크를 통해 계정을 활성화합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "인증 성공",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String uuid) {
        try {
            userService.verify(uuid);
            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(emailTemplates.getEmailVerificationCompletePage());
        } catch (Exception e) {
            String errorHtml = "<h2>인증 실패: " + e.getMessage() + "</h2>" +
                    "<a href='http://localhost:5173/signup'>회원가입 다시하기</a>";
            return ResponseEntity.badRequest()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(errorHtml);
        }
    }

    @Operation(
            summary = "현재 사용자 정보 조회",
            description = "현재 로그인된 사용자의 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "사용자 정보 조회 성공",
                            content = @Content(schema = @Schema(implementation = UserDto.Response.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증되지 않은 사용자",
                            content = @Content(schema = @Schema(implementation = BaseResponse.class))
                    )
            }
    )
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserDto.Response>> getCurrentUser(@AuthenticationPrincipal UserDto.AuthUser authUser) {
        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        UserDto.Response userResponse = userMapper.authUserToResponse(authUser);
        return ResponseEntity.ok(BaseResponse.success(userResponse));
    }

    @Operation(
            summary = "이메일 중복 확인",
            description = "회원가입 시 입력한 이메일이 이미 사용 중인지 확인합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이메일 중복 확인 완료",
                            content = @Content(schema = @Schema(implementation = BaseResponse.class))
                    )
            }
    )
    @GetMapping("/check-email")
    public ResponseEntity<BaseResponse<Map<String, Object>>> checkEmailDuplicate(@RequestParam String email) {
        boolean exists = userRepository.findByEmail(email).isPresent();

        Map<String, Object> data = new HashMap<>();
        data.put("available", !exists);

        if (exists) {
            return ResponseEntity.ok(
                    new BaseResponse<>(false, BaseResponseStatus.EMAIL_NOT_AVAILABLE.getCode(),
                            BaseResponseStatus.EMAIL_NOT_AVAILABLE.getMessage(), data)
            );
        } else {
            return ResponseEntity.ok(
                    new BaseResponse<>(true, BaseResponseStatus.EMAIL_AVAILABLE.getCode(),
                            BaseResponseStatus.EMAIL_AVAILABLE.getMessage(), data)
            );
        }
    }
}
