package org.example.be17pickcook.config.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/user/signup",
            "/api/user/verify",
            "/api/user/check-email",
            "/api/user/find-email",
            "/api/user/request-password-reset",
            "/api/user/reset-password",
            "/oauth2/authorization/kakao",
            "/login/oauth2/code/kakao"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 인증이 필요 없는 경로면 JWT 검증 건너뛰기
        if (isPublicPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie[] cookies = request.getCookies();
        String jwt = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("PICKCOOK_AT".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if (jwt != null) {
            try {
                Claims claims = JwtUtil.getClaims(jwt);

                if (claims != null) {
                    String email = claims.get("email", String.class);
                    String userIdStr = claims.get("idx", String.class);
                    Integer userId = Integer.parseInt(userIdStr);
                    String nickname = claims.get("nickname", String.class);

                    UserDto.AuthUser authUser = UserDto.AuthUser.builder()
                            .idx(userId)
                            .email(email)
                            .nickname(nickname)
                            .enabled(true)
                            .build();

                    // 최소한 빈 권한 리스트 추가
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            authUser,
                            null,
                            List.of()
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("JWT 인증 완료: 사용자 = {}", email);
                }

            } catch (ExpiredJwtException e) {
                log.warn("만료된 JWT: IP={}, URI={}", request.getRemoteAddr(), requestURI);
                clearExpiredCookie(response);
            } catch (Exception e) {
                log.error("JWT 처리 중 오류: IP={}, URI={}, 원인={}", request.getRemoteAddr(), requestURI, e.getMessage());
            }
        } else {
            log.debug("JWT 없음: IP={}, URI={}", request.getRemoteAddr(), requestURI);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String requestURI) {
        return PUBLIC_PATHS.stream().anyMatch(requestURI::startsWith);
    }

    private void clearExpiredCookie(HttpServletResponse response) {
        Cookie expiredCookie = new Cookie("PICKCOOK_AT", null);
        expiredCookie.setMaxAge(0);
        expiredCookie.setPath("/");
        expiredCookie.setHttpOnly(true);
        response.addCookie(expiredCookie);
    }
}
