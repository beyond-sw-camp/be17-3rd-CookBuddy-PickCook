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
 * PickCook 통합 인증 서비스
 * - JWT 토큰 생성/검증/쿠키 관리
 * - 비밀번호 재설정 토큰 관리
 * - 이메일 인증 토큰 관리
 * - 로그아웃 및 세션 관리
 * - 모든 인증 쿠키 삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    // =================================================================
    // 의존성 주입
    // =================================================================

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final EmailVerifyRepository emailVerifyRepository;
    private final PasswordEncoder passwordEncoder;

    // =================================================================
    // 토큰 타입 정의
    // =================================================================

    /**
     * 비밀번호 재설정 토큰 타입별 만료시간 정의
     */
    public enum TokenType {
        EMAIL_RESET(30, TimeUnit.MINUTES),      // 이메일 발송용 (30분)
        INTERNAL_RESET(10, TimeUnit.MINUTES);   // 내부 사용용 (10분)

        private final int duration;
        private final TimeUnit unit;

        TokenType(int duration, TimeUnit unit) {
            this.duration = duration;
            this.unit = unit;
        }

        public int getDuration() {
            return duration;
        }

        public TimeUnit getUnit() {
            return unit;
        }

        /**
         * 토큰 타입별 만료시간 계산
         */
        public LocalDateTime calculateExpiryTime() {
            return LocalDateTime.now().plus(duration, unit.toChronoUnit());
        }
    }

    // =================================================================
    // JWT 토큰 관리
    // =================================================================

    /**
     * JWT 토큰 생성
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
     * @param response HTTP 응답 객체
     * @param jwt JWT 토큰 문자열
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
     * JWT 쿠키 삭제
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
     * @param request HTTP 요청 객체
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

    // =================================================================
    // 비밀번호 재설정 토큰 관리
    // =================================================================

    /**
     * 비밀번호 재설정 토큰 생성 (토큰 타입별 만료시간 적용)
     * @param user 대상 사용자
     * @param tokenType 토큰 타입 (EMAIL_RESET 또는 INTERNAL_RESET)
     * @return 생성된 토큰
     */
    @Transactional
    public String createPasswordResetToken(User user, TokenType tokenType) {
        // 기존 미사용 토큰들 무효화
        passwordResetRepository.markAllAsUsedByUser(user);

        // 새 토큰 생성
        String token = UUID.randomUUID().toString();

        // 토큰 타입별 만료시간 계산
        LocalDateTime expiresAt = tokenType.calculateExpiryTime();

        PasswordReset passwordReset = PasswordReset.builder()
                .email(user.getEmail())
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .build();

        passwordResetRepository.save(passwordReset);

        log.info("비밀번호 재설정 토큰 생성: 사용자 = {}, 타입 = {}, 만료시간 = {}분",
                user.getEmail(), tokenType, tokenType.getDuration());

        return token;
    }

    /**
     * 비밀번호 재설정 토큰 검증
     * @param token 검증할 토큰
     * @return 유효성 여부
     */
    @Transactional(readOnly = true)
    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordReset> resetOptional = passwordResetRepository.findByTokenAndUsedFalse(token);

        if (resetOptional.isPresent()) {
            PasswordReset reset = resetOptional.get();
            return reset.isValid();
        }

        return false;
    }

    /**
     * 비밀번호 재설정 토큰으로 PasswordReset 엔티티 조회
     * @param token 토큰
     * @return PasswordReset 엔티티
     * @throws BaseException 토큰이 유효하지 않은 경우
     */
    @Transactional(readOnly = true)
    public PasswordReset getPasswordResetByToken(String token) {
        return passwordResetRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.INVALID_TOKEN));
    }

    /**
     * 비밀번호 재설정 토큰을 사용 완료로 표시
     * @param reset 사용 완료할 PasswordReset 엔티티
     */
    @Transactional
    public void markPasswordResetTokenAsUsed(PasswordReset reset) {
        reset.markAsUsed();
        passwordResetRepository.save(reset);

        log.info("비밀번호 재설정 토큰 사용 처리 완료: 토큰 = {}", reset.getToken());
    }

    /**
     * 완전한 비밀번호 재설정 처리 (두 시나리오 공통 사용)
     * - 토큰 검증 + 비밀번호 변경 + 토큰 무효화 + JWT 무효화
     * @param token 재설정 토큰
     * @param newPassword 새 비밀번호
     * @param response HTTP 응답 객체
     */
    @Transactional
    public void resetPassword(String token, String newPassword, HttpServletResponse response) {
        // 1. 토큰 검증
        PasswordReset reset = getPasswordResetByToken(token);

        if (!reset.isValid()) {
            throw BaseException.from(BaseResponseStatus.EXPIRED_RESET_TOKEN);
        }

        User user = reset.getUser();

        // 2. 보안: 기존 비밀번호와 동일한지 확인
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw BaseException.from(BaseResponseStatus.SAME_AS_CURRENT_PASSWORD);
        }

        // 3. 새 비밀번호 저장
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);
        userRepository.save(user);

        // 4. 토큰 사용 처리
        markPasswordResetTokenAsUsed(reset);

        // 5. 모든 JWT 토큰 무효화 (두 시나리오 공통)
        clearAllAuthenticationCookies(response);

        log.info("비밀번호 재설정 완료 + JWT 무효화: {}", user.getEmail());
    }

    // =================================================================
    // 이메일 인증 토큰 관리
    // =================================================================

    /**
     * 이메일 인증 토큰 생성
     * @param user 대상 사용자
     * @return 생성된 UUID 토큰
     */
    @Transactional
    public String createEmailVerificationToken(User user) {
        // 기존 미사용 토큰들 삭제
        emailVerifyRepository.deleteByUser(user);

        // 새 토큰 생성
        String uuid = UUID.randomUUID().toString();

        EmailVerify emailVerify = EmailVerify.builder()
                .uuid(uuid)
                .user(user)
                .build(); // expiresAt은 @Builder.Default로 자동 설정 (24시간 후)

        emailVerifyRepository.save(emailVerify);

        log.info("이메일 인증 토큰 생성: 사용자 = {}, 만료시간 = 24시간", user.getEmail());

        return uuid;
    }

    /**
     * 이메일 인증 토큰 검증
     * @param uuid 검증할 UUID 토큰
     * @return 유효성 여부
     */
    @Transactional(readOnly = true)
    public boolean validateEmailVerificationToken(String uuid) {
        Optional<EmailVerify> emailVerifyOptional = emailVerifyRepository.findByUuid(uuid);

        if (emailVerifyOptional.isPresent()) {
            EmailVerify emailVerify = emailVerifyOptional.get();
            return !emailVerify.isExpired();
        }

        return false;
    }

    /**
     * 이메일 인증 토큰으로 EmailVerify 엔티티 조회
     * @param uuid UUID 토큰
     * @return EmailVerify 엔티티
     * @throws BaseException 토큰이 유효하지 않은 경우
     */
    @Transactional(readOnly = true)
    public EmailVerify getEmailVerifyByUuid(String uuid) {
        return emailVerifyRepository.findByUuid(uuid)
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.INVALID_EMAIL_TOKEN));
    }

    /**
     * 이메일 인증을 완료 처리 (토큰 삭제)
     * @param emailVerify 완료 처리할 EmailVerify 엔티티
     */
    @Transactional
    public void markEmailVerificationAsCompleted(EmailVerify emailVerify) {
        emailVerifyRepository.delete(emailVerify);

        log.info("이메일 인증 완료 처리: 사용자 = {}, UUID = {}",
                emailVerify.getUser().getEmail(), emailVerify.getUuid());
    }

    // =================================================================
    // 편의 메서드들 (기존 UserService에서 사용하던 패턴 유지)
    // =================================================================

    /**
     * 이메일 발송용 비밀번호 재설정 토큰 생성 (편의 메서드)
     * - "로그인>비밀번호 찾기"에서 사용
     */
    @Transactional
    public String createEmailPasswordResetToken(User user) {
        return createPasswordResetToken(user, TokenType.EMAIL_RESET);
    }

    /**
     * 내부용 비밀번호 재설정 토큰 생성 (편의 메서드)
     * - "회원정보관리>비밀번호 수정"에서 사용
     */
    @Transactional
    public String createInternalPasswordResetToken(User user) {
        return createPasswordResetToken(user, TokenType.INTERNAL_RESET);
    }

}
