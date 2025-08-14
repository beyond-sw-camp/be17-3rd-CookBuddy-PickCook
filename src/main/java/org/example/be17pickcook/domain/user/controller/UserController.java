package org.example.be17pickcook.domain.user.controller;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserDto.Register dto) throws MessagingException {
        try {
            userService.signup(dto);

            return ResponseEntity.status(200).body("회원가입 성공");

        } catch (RuntimeException e) {

            return ResponseEntity.status(400).body(e.getMessage());
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
            return ResponseEntity.badRequest().body("인증 실패: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto.Response> getCurrentUser(@AuthenticationPrincipal UserDto.AuthUser authUser) {
        if (authUser == null) {
            return ResponseEntity.status(401).build();
        }

        // MapStruct 매퍼 사용 (authUser → Response 변환)
        UserDto.Response userResponse = userMapper.authUserToResponse(authUser);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailDuplicate(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = userRepository.findByEmail(email).isPresent();

        response.put("available", !exists);
        response.put("message", exists ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다.");

        return ResponseEntity.ok(response);
    }
}
