package org.example.be17pickcook.config.oauth;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        UserDto.AuthUser authUser = (UserDto.AuthUser) authentication.getPrincipal();

        log.info("OAuth2 로그인 성공: 사용자 = {}", authUser.getEmail());

        String jwt = JwtUtil.generateToken(authUser.getEmail(), authUser.getIdx(), authUser.getNickname(), authUser.getName());

        if (jwt != null) {
            Cookie cookie = new Cookie("PICKCOOK_AT", jwt);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);

            log.info("OAuth2 JWT 쿠키 설정 완료: 사용자 = {}", authUser.getEmail());

            // 닉네임을 URL 파라미터로 전달 (한글 인코딩 처리)
            String encodedNickname = URLEncoder.encode(authUser.getNickname(), StandardCharsets.UTF_8);
            String redirectUrl = String.format(
                    "https://www.pickcook.kro.kr/?loginSuccess=true&nickname=%s&loginType=social",
                    encodedNickname
            );

            // 프론트엔드 메인 페이지로 리다이렉트
            response.sendRedirect(redirectUrl);

            log.info("OAuth2 리다이렉트 완료: 사용자 = {}", authUser.getEmail());
        } else {
            log.error("OAuth2 JWT 토큰 생성 실패: 사용자 = {}", authUser.getEmail());
            response.sendRedirect("https://admin.pickcook.kro.kr:*/login?error=true");
        }
    }
}
