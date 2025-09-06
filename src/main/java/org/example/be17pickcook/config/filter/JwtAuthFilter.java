package org.example.be17pickcook.config.filter;

import io.jsonwebtoken.ExpiredJwtException;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    // 인증이 필요없는 경로들
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // public 경로는 JWT 검증 건너뛰기
        if (isPublicPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // JWT 토큰 처리
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
                    // 이메일은 claims에서만 가져오기 (subject는 사용 안 함)
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

                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            authUser, null, authUser.getAuthorities()
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ExpiredJwtException e) {
                clearExpiredCookie(response);
                System.out.println("만료된 JWT 토큰이 삭제되었습니다.");
            } catch (Exception e) {
                System.out.println("JWT 처리 중 오류: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    // 누락된 메서드 추가
    private boolean isPublicPath(String requestURI) {
        return PUBLIC_PATHS.stream().anyMatch(requestURI::startsWith);
    }

    // 만료된 쿠키 삭제 메서드 추가
    private void clearExpiredCookie(HttpServletResponse response) {
        Cookie expiredCookie = new Cookie("PICKCOOK_AT", null);
        expiredCookie.setMaxAge(0);
        expiredCookie.setPath("/");
        expiredCookie.setHttpOnly(true);
        response.addCookie(expiredCookie);
    }
}
