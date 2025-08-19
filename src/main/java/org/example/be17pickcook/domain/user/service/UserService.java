package org.example.be17pickcook.domain.user.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.common.exception.BaseException;
import org.example.be17pickcook.domain.user.mapper.UserMapper;
import org.example.be17pickcook.domain.user.model.EmailVerify;
import org.example.be17pickcook.domain.user.model.PasswordReset;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.example.be17pickcook.template.EmailTemplates;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * - PickCook 사용자 서비스
 * - 회원가입/탈퇴, 인증, 프로필 관리, 비밀번호 재설정 등 사용자 관련 모든 기능
 * - BaseException을 사용한 통일된 예외 처리
 * - MapStruct를 활용한 객체 매핑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final UserRepository userRepository;
    private final JavaMailSender emailSender;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailTemplates emailTemplates;
    private final TokenService tokenService;

    // =================================================================
    // Spring Security 인증 관련
    // =================================================================

    /**
     * Spring Security용 사용자 조회 메서드
     * @param username 사용자 이메일 (로그인 ID)
     * @return UserDetails 구현체 (AuthUser)
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        UserDto.AuthUser authUser = userMapper.entityToAuthUser(user);

        return authUser;
    }

    /**
     * 사용자 로그아웃 처리
     * - JWT 쿠키 삭제
     * - OAuth2 세션 무효화
     * - Security Context 초기화
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 🔧 공통 메서드 사용
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

    /**
     * 인증 쿠키 삭제 (회원탈퇴 시 사용)
     */
    public void clearAuthenticationCookies(HttpServletResponse response) {

        clearAllAuthenticationCookies(response);

    }

    // =================================================================
    // 회원가입 및 이메일 인증
    // =================================================================

    /**
     * 회원가입 처리
     * - 이메일 중복 체크
     * - 탈퇴한 계정 재활성화 지원
     * - 이메일 인증 발송
     */
    @Transactional
    public void signup(UserDto.Register dto) throws MessagingException {
        Optional<User> existingUser = userRepository.findByEmail(dto.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            if (user.getDeleted() != null && user.getDeleted()) {
                // 탈퇴한 계정을 재활성화
                log.info("=== 탈퇴한 계정 재활성화 ===");
                log.info("이메일: {}, 기존 닉네임: {} → 새 닉네임: {}",
                        dto.getEmail(), user.getNickname(), dto.getNickname());

                // 계정 복구
                user.restore();

                // 새로운 정보로 업데이트
                user.setNickname(dto.getNickname());
                user.setName(dto.getName());
                user.setPhone(dto.getPhone());
                user.updatePassword(passwordEncoder.encode(dto.getPassword()));

                User savedUser = userRepository.save(user);

                // TokenService로 이메일 인증 토큰 생성 위임
                String uuid = tokenService.createEmailVerificationToken(savedUser);

                sendVerificationEmail(dto.getEmail(), uuid);

                log.info("탈퇴 계정 재활성화 완료 - 사용자: {}", dto.getEmail());
                return;
            } else {
                throw BaseException.from(BaseResponseStatus.DUPLICATE_EMAIL);
            }
        }

        // 새 사용자 가입 로직
        User user = userMapper.registerDtoToEntity(dto);
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        user.updatePassword(encodedPassword);

        User savedUser = userRepository.save(user);

        // TokenService로 이메일 인증 토큰 생성 위임
        String uuid = tokenService.createEmailVerificationToken(savedUser);

        sendVerificationEmail(dto.getEmail(), uuid);
        log.info("새 사용자 회원가입 완료 - 사용자: {}, UUID: {}", dto.getEmail(), uuid);
    }

    /**
     * 이메일 인증 처리
     * @param uuid 이메일 인증 UUID
     */
    @Transactional
    public void verify(String uuid) {
        // TokenService로 토큰 검증 및 조회 위임
        EmailVerify emailVerify = tokenService.getEmailVerifyByUuid(uuid);

        if (emailVerify.isExpired()) {
            throw BaseException.from(BaseResponseStatus.EXPIRED_EMAIL_TOKEN);
        }

        User user = emailVerify.getUser();
        user.userVerify();
        userRepository.save(user);

        // TokenService로 인증 완료 처리 위임
        tokenService.markEmailVerificationAsCompleted(emailVerify);
    }

    // =================================================================
    // 사용자 조회 및 검색
    // =================================================================

    /**
     * 이름과 전화번호로 이메일 찾기 (아이디 찾기)
     */
    @Transactional(readOnly = true)
    public UserDto.FindEmailResponse findEmailByNameAndPhone(UserDto.FindEmailRequest dto) {
        log.info("=== 아이디 찾기 요청 ===");
        log.info("이름: {}, 전화번호: {}", dto.getName(), dto.getPhone());

        User user = userRepository.findByNameAndPhoneAndNotDeleted(dto.getName(), dto.getPhone())
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.USER_NOT_FOUND));

        log.info("찾은 사용자: {}, 이메일: {}", user.getNickname(), user.getEmail());

        String maskedEmail = maskEmail(user.getEmail());

        return UserDto.FindEmailResponse.builder()
                .maskedEmail(maskedEmail)
                .email(user.getEmail())
                .build();
    }

    /**
     * 현재 사용자 정보 조회 (Controller용)
     * - 데이터베이스에서 최신 정보 조회
     * - Response DTO로 변환하여 반환
     * @param userId 사용자 ID
     * @return 사용자 응답 DTO
     */
    @Transactional(readOnly = true)
    public UserDto.Response getCurrentUserInfo(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.USER_NOT_FOUND));

        return userMapper.entityToResponse(user);
    }

    /**
     * 닉네임 사용 가능 여부 확인
     * @param nickname 확인할 닉네임
     * @param currentUserId 현재 사용자 ID (본인 닉네임은 허용)
     * @return 사용 가능 여부
     */
    public boolean isNicknameAvailable(String nickname, Integer currentUserId) {
        Optional<User> existingUser = userRepository.findByNicknameAndNotDeleted(nickname);

        if (existingUser.isEmpty()) {
            return true; // 사용 가능
        }

        User user = existingUser.get();

        // 현재 사용자의 닉네임은 사용 가능으로 처리
        if (currentUserId != null && user.getIdx().equals(currentUserId)) {
            return true; // 본인의 기존 닉네임
        }

        return false; // 다른 사용자가 사용 중
    }

    /**
     * 이메일 중복 확인
     * @param email 확인할 이메일
     * @return 사용 가능 여부가 포함된 응답 Map
     */
    @Transactional(readOnly = true)
    public Map<String, Object> checkEmailAvailability(String email) {
        boolean exists = userRepository.findByEmailAndNotDeleted(email).isPresent();
        return Map.of("available", !exists);
    }

    // =================================================================
    // 회원정보 수정
    // =================================================================

    /**
     * 사용자 프로필 정보 수정
     * - 닉네임 변경 시 중복 체크
     * - MapStruct를 활용한 Entity 업데이트
     */
    @Transactional
    public UserDto.Response updateProfile(Integer userId, UserDto.UpdateProfile dto) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.USER_NOT_FOUND));

        // 닉네임이 변경되었을 때만 중복 체크
        if (dto.getNickname() != null && !dto.getNickname().equals(user.getNickname())) {
            // 닉네임 유효성 검사
            String trimmedNickname = dto.getNickname().trim();
            if (trimmedNickname.length() < 2 || trimmedNickname.length() > 20) {
                throw BaseException.from(BaseResponseStatus.INVALID_NICKNAME_LENGTH);
            }

            // 중복 체크 (다른 사용자가 사용 중인지)
            if (userRepository.findByNicknameAndNotDeleted(trimmedNickname).isPresent()) {
                throw BaseException.from(BaseResponseStatus.NICKNAME_NOT_AVAILABLE);
            }

            log.info("닉네임 중복 체크 통과: {} -> {}", user.getNickname(), trimmedNickname);
        } else {
            log.info("닉네임 변경 없음 - 중복 체크 건너뛰기");
        }

        // MapStruct를 사용한 Entity 업데이트
        userMapper.updateEntityFromDto(user, dto);

        // 데이터베이스 저장
        User savedUser = userRepository.save(user);

        // Response DTO 변환
        return userMapper.entityToResponse(savedUser);
    }

    // =================================================================
    // 비밀번호 재설정
    // =================================================================

    /**
     * 비밀번호 재설정 요청 처리 (이메일 발송)
     * - 존재하지 않는 이메일도 보안상 동일하게 응답
     * - TokenService를 사용한 토큰 생성
     */
    @Transactional
    public void requestPasswordReset(String email) throws MessagingException {
        log.info("=== 비밀번호 재설정 요청 ===");
        log.info("이메일: {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // TokenService로 토큰 생성 위임
            String token = tokenService.createPasswordResetToken(user);

            // 이메일 발송
            sendPasswordResetEmail(email, token);

            log.info("비밀번호 재설정 이메일 발송 완료: {}", email);
        } else {
            log.info("존재하지 않는 이메일이지만 보안상 성공 응답: {}", email);
        }
    }

    /**
     * 마이페이지용 비밀번호 변경 토큰 생성 (이메일 발송 없음)
     */
    @Transactional
    public String generatePasswordChangeToken(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.USER_NOT_FOUND));

        // TokenService로 토큰 생성 위임
        String token = tokenService.createPasswordResetToken(user);

        log.info("마이페이지 비밀번호 변경 토큰 생성 완료: 사용자 ID = {}", userId);

        return token;
    }

    /**
     * 비밀번호 재설정 토큰 검증
     */
    @Transactional(readOnly = true)
    public boolean validateResetToken(String token) {
        // TokenService로 검증 위임
        return tokenService.validatePasswordResetToken(token);
    }

    /**
     * 새 비밀번호로 재설정
     * - 기존 비밀번호와 동일한지 확인
     * - 토큰 사용 처리
     */
    @Transactional
    public void resetPassword(String token, String newPassword, HttpServletResponse response) {
        // TokenService로 토큰 조회 위임
        PasswordReset reset = tokenService.getPasswordResetByToken(token);

        if (!reset.isValid()) {
            throw BaseException.from(BaseResponseStatus.EXPIRED_RESET_TOKEN);
        }

        User user = reset.getUser();

        // 보안: 기존 비밀번호와 동일한지 확인
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw BaseException.from(BaseResponseStatus.SAME_AS_CURRENT_PASSWORD);
        }

        // 새 비밀번호 저장
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);
        userRepository.save(user);

        // TokenService로 토큰 사용 처리 위임
        tokenService.markPasswordResetTokenAsUsed(reset);

        // 기존 JWT 토큰 무효화를 위한 쿠키 삭제
        clearAllAuthenticationCookies(response);

        log.info("비밀번호 재설정 완료: {}", user.getEmail());
    }

    /**
     * 모든 인증 관련 쿠키 삭제 (공통 메서드)
     */
    public void clearAllAuthenticationCookies(HttpServletResponse response) {
        // JWT 쿠키 삭제
        Cookie jwtCookie = new Cookie("PICKCOOK_AT", null);
        jwtCookie.setMaxAge(0);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        response.addCookie(jwtCookie);

        // 세션 쿠키 삭제
        Cookie sessionCookie = new Cookie("JSESSIONID", null);
        sessionCookie.setMaxAge(0);
        sessionCookie.setPath("/");
        sessionCookie.setHttpOnly(true);
        response.addCookie(sessionCookie);

        log.info("모든 인증 쿠키 삭제 완료");
    }

    // =================================================================
    // 회원탈퇴
    // =================================================================

    /**
     * 회원탈퇴 처리
     * - 소프트 삭제 (실제 데이터는 보존)
     * - 비밀번호 확인 (일반 로그인 사용자만)
     * - OAuth2 사용자 지원
     */
    @Transactional
    public UserDto.WithdrawResponse withdrawUser(Integer userId, UserDto.WithdrawRequest dto) {
        log.info("=== 회원탈퇴 요청 ===");
        log.info("사용자 ID: {}, 탈퇴 사유: {}", userId, dto.getReason());

        try {
            // 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BaseException.from(BaseResponseStatus.USER_NOT_FOUND));

            log.info("사용자 조회 완료 - 이메일: {}, 삭제상태: {}", user.getEmail(), user.getDeleted());

            // 이미 탈퇴한 사용자인지 확인
            if (user.getDeleted() != null && user.getDeleted()) {
                log.warn("이미 탈퇴한 사용자: {}", user.getEmail());
                throw BaseException.from(BaseResponseStatus.ALREADY_WITHDRAWN);
            }

            // 탈퇴 확인 체크
            if (dto.getConfirmWithdraw() == null || !dto.getConfirmWithdraw()) {
                log.warn("탈퇴 확인 체크 실패");
                throw BaseException.from(BaseResponseStatus.WITHDRAW_CONFIRM_REQUIRED);
            }

            log.info("탈퇴 확인 체크 통과");

            // 비밀번호 확인 (일반 로그인 사용자만)
            if (user.getPassword() != null && !isOAuth2User(user)) {
                log.info("일반 로그인 사용자 - 비밀번호 확인 시작");

                if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
                    log.warn("비밀번호가 입력되지 않음");
                    throw BaseException.of(BaseResponseStatus.INVALID_USER_INFO, "비밀번호를 입력해주세요.");
                }

                if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                    log.warn("비밀번호 불일치");
                    throw BaseException.from(BaseResponseStatus.INVALID_USER_INFO);
                }

                log.info("비밀번호 확인 완료");
            } else {
                log.info("소셜 로그인 사용자 - 비밀번호 확인 건너뛰기");
            }

            // 소프트 삭제 실행
            log.info("소프트 삭제 실행 시작");
            user.softDelete();

            log.info("사용자 저장 시작");
            User savedUser = userRepository.save(user);

            log.info("회원탈퇴 완료 - 사용자: {}, 탈퇴일시: {}", savedUser.getEmail(), savedUser.getDeletedAt());

            // 마스킹된 이메일로 응답
            String maskedEmail = maskEmail(user.getEmail());
            log.info("마스킹된 이메일: {}", maskedEmail);

            UserDto.WithdrawResponse response = UserDto.WithdrawResponse.builder()
                    .message("회원탈퇴가 완료되었습니다.")
                    .withdrawnAt(user.getDeletedAt())
                    .email(maskedEmail)
                    .build();

            log.info("응답 객체 생성 완료");
            return response;

        } catch (BaseException e) {
            log.error("회원탈퇴 처리 중 BaseException 발생", e);
            throw e; // BaseException은 그대로 다시 던짐
        } catch (Exception e) {
            log.error("회원탈퇴 처리 중 예상하지 못한 오류 발생", e);
            throw BaseException.of(BaseResponseStatus.SERVER_ERROR, "회원탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    // =================================================================
    // 헬퍼 메서드들 (Private)
    // =================================================================

    /**
     * 이메일 마스킹 처리
     * 예: test123@gmail.com → tes***@gmail.com
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 3) {
            return localPart.charAt(0) + "***@" + domain;
        } else {
            return localPart.substring(0, 3) + "***@" + domain;
        }
    }

    /**
     * OAuth2 사용자 판별
     * 이메일이 숫자로만 구성된 경우 OAuth2 사용자로 판별 (카카오 ID)
     */
    private boolean isOAuth2User(User user) {
        return user.getEmail() != null && user.getEmail().matches("^\\d+$");
    }

    /**
     * 회원가입 인증 이메일 발송
     */
    private void sendVerificationEmail(String email, String uuid) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("[PickCook] 이메일 인증을 완료해주세요");
        helper.setFrom("noreply@pickcook.com");

        String htmlContent = emailTemplates.getEmailVerificationTemplate(email, uuid);
        helper.setText(htmlContent, true);

        emailSender.send(message);
    }

    /**
     * 비밀번호 재설정 이메일 발송
     */
    private void sendPasswordResetEmail(String email, String token) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("[PickCook] 비밀번호 재설정 요청");
        helper.setFrom("noreply@pickcook.com");

        String htmlContent = emailTemplates.getPasswordResetEmailTemplate(email, token);
        helper.setText(htmlContent, true);

        emailSender.send(message);
    }
}