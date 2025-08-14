package org.example.be17pickcook.domain.user.controller;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.template.EmailTemplates;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.example.be17pickcook.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.example.be17pickcook.domain.user.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final EmailTemplates emailTemplates;
    private final UserRepository userRepository;

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
    public ResponseEntity<String> verify(String uuid) {
        System.out.println("이메일 인증 요청 받음: " + uuid);
        userService.verify(uuid);

        String htmlContent = emailTemplates.getEmailVerificationCompletePage();

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(htmlContent);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto.UserRes> getCurrentUser(@AuthenticationPrincipal UserDto.AuthUser authUser) {
        if (authUser != null) {
            UserDto.UserRes userRes = UserDto.UserRes.builder()
                    .idx(authUser.getIdx())
                    .email(authUser.getEmail())
                    .nickname(authUser.getNickname())
                    .build();

            return ResponseEntity.ok(userRes);
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailDuplicate(@RequestParam String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        Map<String, Object> response = new HashMap<>();

        if (existingUser.isPresent()) {
            response.put("available", false);
            response.put("message", "이미 사용 중인 이메일입니다.");
        } else {
            response.put("available", true);
            response.put("message", "사용 가능한 이메일입니다.");
        }

        return ResponseEntity.ok(response);
    }
}
