package org.example.be17pickcook.config.oauth;

import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.utils.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        System.out.println("=== OAuth2 로그인 성공 ===");
        UserDto.AuthUser authUser = (UserDto.AuthUser) authentication.getPrincipal();

        // 디버깅 로그 추가
        System.out.println("사용자 정보:");
        System.out.println("- idx: " + authUser.getIdx());
        System.out.println("- email: " + authUser.getEmail());
        System.out.println("- nickname: " + authUser.getNickname());

        String jwt = JwtUtil.generateToken(authUser.getEmail(), authUser.getIdx());

        if (jwt != null) {
            Cookie cookie = new Cookie("PICKCOOK_AT", jwt);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            System.out.println("OAuth2 쿠키 설정 완료");

            // 닉네임을 URL 파라미터로 전달 (한글 인코딩 처리)
            String encodedNickname = URLEncoder.encode(authUser.getNickname(), StandardCharsets.UTF_8);
            String redirectUrl = String.format(
                    "http://localhost:5173/?loginSuccess=true&nickname=%s&loginType=social",
                    encodedNickname
            );

            System.out.println("리다이렉트 URL: " + redirectUrl);

            // 프론트엔드 메인 페이지로 리다이렉트
            response.sendRedirect(redirectUrl);
            System.out.println("OAuth2 리다이렉트 완료");
        } else {
            System.out.println("JWT 생성 실패");
            response.sendRedirect("http://localhost:5173/login?error=true");
        }
    }
}
