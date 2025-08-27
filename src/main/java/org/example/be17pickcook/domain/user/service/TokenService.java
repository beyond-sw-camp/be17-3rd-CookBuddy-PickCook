package org.example.be17pickcook.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.common.exception.BaseException;
import org.example.be17pickcook.domain.user.model.EmailVerify;
import org.example.be17pickcook.domain.user.model.PasswordReset;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.repository.EmailVerifyRepository;
import org.example.be17pickcook.domain.user.repository.PasswordResetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * PickCook 토큰 관리 서비스
 * - 비밀번호 재설정 토큰
 * - 이메일 인증 토큰
 * - JWT 토큰 (향후 확장)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final PasswordResetRepository passwordResetRepository;
    private final EmailVerifyRepository emailVerifyRepository;

    // =================================================================
    // 토큰 타입 정의
    // =================================================================

    /**
     * 비밀번호 재설정 토큰 타입별 만료시간 정의
     */
    public enum TokenType {
        EMAIL_RESET(30),      // 이메일 발송용 - 30분
        INTERNAL_RESET(10);   // 마이페이지용 - 10분

        private final int expirationMinutes;

        TokenType(int expirationMinutes) {
            this.expirationMinutes = expirationMinutes;
        }

        public int getExpirationMinutes() {
            return expirationMinutes;
        }
    }

    // =================================================================
    // 비밀번호 재설정 토큰 관련
    // =================================================================

    /**
     * 비밀번호 재설정 토큰 생성 (타입별 만료시간 적용)
     * @param user 대상 사용자
     * @param tokenType 토큰 타입 (EMAIL_RESET | INTERNAL_RESET)
     * @return 생성된 토큰
     */
    @Transactional
    public String createPasswordResetToken(User user, TokenType tokenType) {
        // 기존 미사용 토큰들 무효화
        passwordResetRepository.markAllAsUsedByUser(user);

        // 새 토큰 생성
        String token = UUID.randomUUID().toString();

        // 토큰 타입별 만료시간 계산
        LocalDateTime expirationTime = LocalDateTime.now()
                .plusMinutes(tokenType.getExpirationMinutes());

        PasswordReset passwordReset = PasswordReset.builder()
                .email(user.getEmail())
                .token(token)
                .user(user)
                .expiresAt(expirationTime)
                .build();

        passwordResetRepository.save(passwordReset);

        log.info("비밀번호 재설정 토큰 생성: 사용자 = {}, 타입 = {}, 만료시간 = {}분",
                user.getEmail(), tokenType, tokenType.getExpirationMinutes());

        return token;
    }

    /**
     * 이메일 발송용 비밀번호 재설정 토큰 생성 (편의 메서드)
     */
    @Transactional
    public String createEmailPasswordResetToken(User user) {
        return createPasswordResetToken(user, TokenType.EMAIL_RESET);
    }

    /**
     * 마이페이지용 비밀번호 재설정 토큰 생성 (편의 메서드)
     */
    @Transactional
    public String createInternalPasswordResetToken(User user) {
        return createPasswordResetToken(user, TokenType.INTERNAL_RESET);
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

    // =================================================================
    // 이메일 인증 토큰 관련
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
    // JWT 토큰 관련 (향후 확장)
    // =================================================================

    // TODO: 이후 JWT 토큰 관련 메서드들 이관 예정 (선택사항)
}