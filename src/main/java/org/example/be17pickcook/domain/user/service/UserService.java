package org.example.be17pickcook.domain.user.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.domain.user.mapper.EmailVerifyMapper;
import org.example.be17pickcook.domain.user.mapper.UserMapper;
import org.example.be17pickcook.domain.user.model.EmailVerify;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.user.repository.EmailVerifyRepository;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final EmailVerifyRepository emailVerifyRepository;
    private final JavaMailSender emailSender;
    // MapStruct 매퍼들 주입
    private final UserMapper userMapper;
    private final EmailVerifyMapper emailVerifyMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // MapStruct 매퍼 사용
        return userMapper.entityToAuthUser(user);
    }

    @Transactional
    public void signup(UserDto.Register dto) throws MessagingException {
        // 이메일 중복 체크
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // MapStruct 매퍼로 Entity 변환
        User user = userMapper.registerDtoToEntity(dto);
        User savedUser = userRepository.save(user);

        // UUID 생성 및 이메일 인증 데이터 저장
        String uuid = UUID.randomUUID().toString();

        // MapStruct 매퍼로 EmailVerify 생성
        EmailVerify emailVerify = emailVerifyMapper.createEmailVerify(uuid, savedUser);
        emailVerifyRepository.save(emailVerify);

        // 인증 이메일 발송
        sendVerificationEmail(dto.getEmail(), uuid);

        log.info("회원가입 완료 - 사용자: {}, UUID: {}", dto.getEmail(), uuid);
    }

    @Transactional
    public void verify(String uuid) {
        EmailVerify emailVerify = emailVerifyRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 코드입니다."));

        // 만료 시간 체크 (Entity의 비즈니스 로직 사용)
        if (emailVerify.isExpired()) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
        }

        User user = emailVerify.getUser();
        user.userVerify(); // 계정 활성화
        userRepository.save(user);

        log.info("이메일 인증 완료 - 사용자: {}", user.getEmail());
    }

    // 이메일 발송 로직 분리
    private void sendVerificationEmail(String email, String uuid) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("[PickCook] 이메일 인증을 완료해주세요");

        String htmlContent = buildEmailContent(uuid);
        helper.setText(htmlContent, true);

        emailSender.send(message);
    }

    // 이메일 템플릿 분리
    private String buildEmailContent(String uuid) {
        return "<h2 style='color: #2e6c80;'>PickCook 가입을 환영합니다!</h2>"
                + "<p>아래 링크를 클릭하여 이메일 인증을 완료해주세요:</p>"
                + "<a href='http://localhost:8080/api/user/verify?uuid=" + uuid + "' "
                + "style='background-color: #2e6c80; color: white; padding: 10px 20px; "
                + "text-decoration: none; border-radius: 5px;'>이메일 인증하기</a>"
                + "<p><small>이 링크는 24시간 후 만료됩니다.</small></p>";
    }
}
