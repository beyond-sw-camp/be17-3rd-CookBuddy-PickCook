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

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserDto.Response>> getCurrentUser(@AuthenticationPrincipal UserDto.AuthUser authUser) {
        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        UserDto.Response userResponse = userMapper.authUserToResponse(authUser);
        return ResponseEntity.ok(BaseResponse.success(userResponse));
    }

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
