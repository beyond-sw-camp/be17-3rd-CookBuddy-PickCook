package org.example.be17pickcook.domain.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.template.EmailTemplates;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * PickCook 이메일 발송 전담 서비스
 * - 회원가입 인증 이메일 발송
 * - 비밀번호 재설정 이메일 발송
 * - 이메일 템플릿 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final JavaMailSender emailSender;
    private final EmailTemplates emailTemplates;

    // =================================================================
    // 회원가입 인증 이메일 발송
    // =================================================================

    /**
     * 회원가입 인증 이메일 발송
     */
    public void sendVerificationEmail(String email, String uuid) throws MessagingException {
        String htmlContent = emailTemplates.getEmailVerificationTemplate(email, uuid);
        sendEmail(email, "[PickCook] 이메일 인증을 완료해주세요", htmlContent);
        log.info("회원가입 인증 이메일 발송 완료: {}", email);
    }

    // =================================================================
    // 비밀번호 재설정 이메일 발송
    // =================================================================

    /**
     * 비밀번호 재설정 이메일 발송
     */
    public void sendPasswordResetEmail(String email, String token) throws MessagingException {
        String htmlContent = emailTemplates.getPasswordResetEmailTemplate(email, token);
        sendEmail(email, "[PickCook] 비밀번호 재설정 요청", htmlContent);
        log.info("비밀번호 재설정 이메일 발송 완료: {}", email);
    }

    // =================================================================
    // 공통 이메일 발송 메서드 (중복 제거)
    // =================================================================

    /**
     * 공통 이메일 발송 메서드
     * @param email 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param htmlContent HTML 콘텐츠
     * @throws MessagingException 이메일 발송 실패 시
     */
    private void sendEmail(String email, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // 이메일 기본 정보 설정
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setFrom("noreply@pickcook.com");
        helper.setText(htmlContent, true);  // true = HTML 형식

        // 이메일 발송
        emailSender.send(message);
    }


    // =================================================================
    // 편의 메서드들 (향후 확장성을 위한 래핑)
    // =================================================================

    /**
     * 이메일 발송 가능 여부 확인 (향후 확장용)
     * @return 항상 true (현재는 단순 구현)
     */
    public boolean isEmailServiceAvailable() {
        return true;  // 향후 SMTP 서버 상태 체크 등으로 확장 가능
    }

    /**
     * 이메일 템플릿 유효성 검증 (향후 확장용)
     * @param templateType 템플릿 타입
     * @return 항상 true (현재는 단순 구현)
     */
    public boolean validateEmailTemplate(String templateType) {
        return true;  // 향후 템플릿 존재 여부 체크 등으로 확장 가능
    }

}
