package org.example.be17pickcook.domain.user.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.common.exception.BaseException;
import org.example.be17pickcook.domain.user.model.EmailVerify;
import org.example.be17pickcook.domain.user.model.PasswordReset;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.repository.EmailVerifyRepository;
import org.example.be17pickcook.domain.user.repository.PasswordResetRepository;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.example.be17pickcook.utils.JwtUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * PickCook 인증/인가 전담 서비스
 * - JWT 쿠키 생성, 삭제, 관리
 * - 로그아웃 및 세션 관리
 * - Spring Security 연동 처리
 *
 * 토큰 생성/검증은 TokenService에서 담당
 * 순수 인증 상태 관리에만 집중
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    // =================================================================
    // 의존성 주입
    // =================================================================

    private final UserRepository userRepository;

    // =================================================================
    // JWT 토큰 관리
    // =================================================================

    /**
     * JWT 토큰 생성
     *
     * @param user 대상 사용자
     * @return 생성된 JWT 토큰
     */
    public String generateJwtToken(User user) {
        return JwtUtil.generateToken(
                user.getEmail(),
                user.getIdx(),
                user.getNickname(),
                user.getName()
        );
    }

    /**
     * JWT 토큰을 HttpOnly 쿠키로 설정
     *
     * @param response HTTP 응답 객체
     * @param jwt      JWT 토큰 문자열
     */
    public void setJwtCookie(HttpServletResponse response, String jwt) {
        String cookieValue = String.format(
                "PICKCOOK_AT=%s; Path=/; HttpOnly; Max-Age=7200; SameSite=Lax",
                jwt
        );
        response.setHeader("Set-Cookie", cookieValue);

        log.info("JWT 쿠키 설정 완료 - 만료시간: 120분");
    }

    /**
     * JWT 토큰 생성 및 쿠키 설정 (편의 메서드)
     * @param user 대상 사용자
     * @param response HTTP 응답 객체
     */
    public void createJwtCookie(User user, HttpServletResponse response) {
        String jwt = generateJwtToken(user);
        setJwtCookie(response, jwt);
        log.info("사용자 로그인 JWT 쿠키 생성: {}", user.getEmail());
    }

    /**
     * JWT 쿠키 삭제
     *
     * @param response HTTP 응답 객체
     */
    public void clearJwtCookie(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("PICKCOOK_AT", null);
        jwtCookie.setMaxAge(0);  // 즉시 만료
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        response.addCookie(jwtCookie);

        log.info("JWT 쿠키 삭제 완료");
    }

    /**
     * 모든 인증 관련 쿠키 삭제 (로그아웃, 비밀번호 변경 시 사용)
     *
     * @param response HTTP 응답 객체
     */
    public void clearAllAuthenticationCookies(HttpServletResponse response) {
        // JWT 쿠키 삭제
        clearJwtCookie(response);

        // 세션 쿠키 삭제
        Cookie sessionCookie = new Cookie("JSESSIONID", null);
        sessionCookie.setMaxAge(0);
        sessionCookie.setPath("/");
        sessionCookie.setHttpOnly(true);
        response.addCookie(sessionCookie);

        log.info("모든 인증 쿠키 삭제 완료 (JWT + Session)");
    }

    /**
     * 완전한 로그아웃 처리
     * - 쿠키 삭제 + 세션 무효화 + Security Context 초기화
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 모든 인증 쿠키 삭제
        clearAllAuthenticationCookies(response);

        // 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            log.info("OAuth2 세션 무효화 완료");
        }

        // Security Context 초기화
        SecurityContextHolder.clearContext();
        log.info("Security Context 초기화 완료");
    }
}
