package org.example.be17pickcook.domain.user.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.domain.user.mapper.EmailVerifyMapper;
import org.example.be17pickcook.domain.user.mapper.UserMapper;
import org.example.be17pickcook.domain.user.model.EmailVerify;
import org.example.be17pickcook.domain.user.model.PasswordReset;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.user.repository.EmailVerifyRepository;
import org.example.be17pickcook.domain.user.repository.PasswordResetRepository;
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
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmailVerifyRepository emailVerifyRepository;
    private final PasswordResetRepository passwordResetRepository; // 🔧 추가
    private final JavaMailSender emailSender;
    private final UserMapper userMapper;
    private final EmailVerifyMapper emailVerifyMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailTemplates emailTemplates;

    // 🔧 추가: 로그아웃 처리 메서드
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // JWT 쿠키 삭제
        Cookie jwtCookie = new Cookie("PICKCOOK_AT", null);
        jwtCookie.setMaxAge(0);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        response.addCookie(jwtCookie);

        // JSESSIONID 쿠키 삭제 (OAuth2 세션)
        Cookie sessionCookie = new Cookie("JSESSIONID", null);
        sessionCookie.setMaxAge(0);
        sessionCookie.setPath("/");
        sessionCookie.setHttpOnly(true);
        response.addCookie(sessionCookie);

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

    // 🔧 추가: 닉네임 사용 가능 여부 확인
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

    // 🔧 추가: 인증 쿠키 삭제 (탈퇴 시 사용)
    public void clearAuthenticationCookies(HttpServletResponse response) {
        // JWT 쿠키 삭제
        Cookie jwtCookie = new Cookie("PICKCOOK_AT", null);
        jwtCookie.setMaxAge(0);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        response.addCookie(jwtCookie);

        log.info("인증 쿠키 삭제 완료");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 🔧 탈퇴한 계정 체크
        if (user.getDeleted() != null && user.getDeleted()) {
            throw new UsernameNotFoundException("탈퇴한 계정입니다. 동일한 이메일로 재가입하시면 계정이 복구됩니다.");
        }

        return userMapper.entityToAuthUser(user);
    }

    @Transactional
    public void signup(UserDto.Register dto) throws MessagingException {
        Optional<User> existingUser = userRepository.findByEmail(dto.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            if (user.getDeleted() != null && user.getDeleted()) {
                // 🔧 탈퇴한 계정을 재활성화
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

                // 기존 이메일 인증 삭제
                emailVerifyRepository.deleteByUser(savedUser);

                // 새로운 이메일 인증 발송
                String uuid = UUID.randomUUID().toString();
                EmailVerify emailVerify = emailVerifyMapper.createEmailVerify(uuid, savedUser);
                emailVerifyRepository.save(emailVerify);

                sendVerificationEmail(dto.getEmail(), uuid);

                log.info("탈퇴 계정 재활성화 완료 - 사용자: {}", dto.getEmail());
                return;
            } else {
                // 활성 계정이면 중복 에러
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
        }

        // 🔧 새 사용자 가입 로직 (기존과 동일)
        User user = userMapper.registerDtoToEntity(dto);
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        user.updatePassword(encodedPassword);

        User savedUser = userRepository.save(user);

        String uuid = UUID.randomUUID().toString();
        EmailVerify emailVerify = emailVerifyMapper.createEmailVerify(uuid, savedUser);
        emailVerifyRepository.save(emailVerify);

        sendVerificationEmail(dto.getEmail(), uuid);
        log.info("새 사용자 회원가입 완료 - 사용자: {}, UUID: {}", dto.getEmail(), uuid);
    }

    @Transactional
    public void verify(String uuid) {
        EmailVerify emailVerify = emailVerifyRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 코드입니다."));

        if (emailVerify.isExpired()) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
        }

        User user = emailVerify.getUser();
        user.userVerify();
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserDto.FindEmailResponse findEmailByNameAndPhone(UserDto.FindEmailRequest dto) {
        log.info("=== 아이디 찾기 요청 ===");
        log.info("이름: {}, 전화번호: {}", dto.getName(), dto.getPhone());

        // 184라인 수정
        User user = userRepository.findByNameAndPhoneAndNotDeleted(dto.getName(), dto.getPhone())
                .orElseThrow(() -> new IllegalArgumentException("입력하신 정보와 일치하는 계정을 찾을 수 없습니다."));

        log.info("찾은 사용자: {}, 이메일: {}", user.getNickname(), user.getEmail());

        String maskedEmail = maskEmail(user.getEmail());

        return UserDto.FindEmailResponse.builder()
                .maskedEmail(maskedEmail)
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public UserDto.Response updateProfile(Integer userId, UserDto.UpdateProfile dto) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 닉네임이 변경되었을 때만 중복 체크
        if (dto.getNickname() != null && !dto.getNickname().equals(user.getNickname())) {
            // 닉네임 유효성 검사
            String trimmedNickname = dto.getNickname().trim();
            if (trimmedNickname.length() < 2 || trimmedNickname.length() > 20) {
                throw new IllegalArgumentException("닉네임은 2자 이상 20자 이하로 입력해주세요.");
            }

            // 중복 체크 (다른 사용자가 사용 중인지)
            if (userRepository.findByNicknameAndNotDeleted(trimmedNickname).isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
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

    // 🔧 추가: 비밀번호 재설정 요청
    @Transactional
    public void requestPasswordReset(String email) throws MessagingException {
        log.info("=== 비밀번호 재설정 요청 ===");
        log.info("이메일: {}", email);

        // 사용자 조회 (존재하지 않아도 에러 안 남)
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 기존 미사용 토큰들 무효화
            passwordResetRepository.markAllAsUsedByUser(user);

            // 새 토큰 생성
            String token = UUID.randomUUID().toString();

            PasswordReset passwordReset = PasswordReset.builder()
                    .email(email)
                    .token(token)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(30)) // 30분 후 만료
                    .build();

            passwordResetRepository.save(passwordReset);

            // 실제 이메일 발송
            sendPasswordResetEmail(email, token);

            log.info("비밀번호 재설정 이메일 발송 완료: {}", email);
        } else {
            // 🔧 보안: 존재하지 않는 이메일이어도 동일한 응답
            log.info("존재하지 않는 이메일이지만 보안상 성공 응답: {}", email);
        }
    }

    // 🔧 추가: 토큰 검증
    @Transactional(readOnly = true)
    public boolean validateResetToken(String token) {
        Optional<PasswordReset> resetOptional = passwordResetRepository.findByTokenAndUsedFalse(token);

        if (resetOptional.isPresent()) {
            PasswordReset reset = resetOptional.get();
            return reset.isValid();
        }

        return false;
    }

    // 🔧 추가: 새 비밀번호 저장
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordReset reset = passwordResetRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

        if (!reset.isValid()) {
            throw new IllegalArgumentException("만료되거나 이미 사용된 토큰입니다.");
        }

        User user = reset.getUser();

        // 🔧 보안: 기존 비밀번호와 동일한지 확인
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호와 동일합니다. 다른 비밀번호를 사용해주세요.");
        }

        // 새 비밀번호 저장
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);
        userRepository.save(user);

        // 토큰 사용 처리
        reset.markAsUsed();
        passwordResetRepository.save(reset);

        log.info("비밀번호 재설정 완료: {}", user.getEmail());
    }

    // 🔧 이메일 마스킹 헬퍼 메서드
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

    // 🔧 비밀번호 재설정 이메일 발송
    private void sendPasswordResetEmail(String email, String token) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("[PickCook] 비밀번호 재설정 요청");
        helper.setFrom("noreply@pickcook.com");

        // 🔧 수정: EmailTemplates에서 HTML 템플릿 가져오기
        String htmlContent = emailTemplates.getPasswordResetEmailTemplate(email, token);
        helper.setText(htmlContent, true);

        emailSender.send(message);
    }

    private void sendVerificationEmail(String email, String uuid) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("[PickCook] 이메일 인증을 완료해주세요");
        helper.setFrom("noreply@pickcook.com");

        // 🔧 수정: EmailTemplates에서 HTML 템플릿 가져오기
        String htmlContent = emailTemplates.getEmailVerificationTemplate(email, uuid);
        helper.setText(htmlContent, true);

        emailSender.send(message);
    }

    // 🔧 추가: OAuth2 사용자 판별 헬퍼 메서드
    private boolean isOAuth2User(User user) {
        // 이메일이 숫자로만 구성된 경우 OAuth2 사용자로 판별
        return user.getEmail() != null && user.getEmail().matches("^\\d+$");
    }

    @Transactional
    public UserDto.WithdrawResponse withdrawUser(Integer userId, UserDto.WithdrawRequest dto) {
        log.info("=== 회원탈퇴 요청 ===");
        log.info("사용자 ID: {}, 탈퇴 사유: {}", userId, dto.getReason());

        try {
            // 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            log.info("사용자 조회 완료 - 이메일: {}, 삭제상태: {}", user.getEmail(), user.getDeleted());

            // 이미 탈퇴한 사용자인지 확인
            if (user.getDeleted() != null && user.getDeleted()) {
                log.warn("이미 탈퇴한 사용자: {}", user.getEmail());
                throw new IllegalArgumentException("이미 탈퇴한 계정입니다.");
            }

            // 탈퇴 확인 체크
            if (dto.getConfirmWithdraw() == null || !dto.getConfirmWithdraw()) {
                log.warn("탈퇴 확인 체크 실패");
                throw new IllegalArgumentException("탈퇴 확인이 필요합니다.");
            }

            log.info("탈퇴 확인 체크 통과");

            // 비밀번호 확인 (일반 로그인 사용자만)
            if (user.getPassword() != null && !isOAuth2User(user)) {
                log.info("일반 로그인 사용자 - 비밀번호 확인 시작");

                if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
                    log.warn("비밀번호가 입력되지 않음");
                    throw new IllegalArgumentException("비밀번호를 입력해주세요.");
                }

                if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                    log.warn("비밀번호 불일치");
                    throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
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

        } catch (Exception e) {
            log.error("회원탈퇴 처리 중 예외 발생", e);
            throw e; // 예외를 다시 던져서 Controller에서 처리
        }
    }

}