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
    private final UserMapper userMapper;
    private final EmailVerifyMapper emailVerifyMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        return userMapper.entityToAuthUser(user);
    }

    @Transactional
    public void signup(UserDto.Register dto) throws MessagingException {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = userMapper.registerDtoToEntity(dto);
        User savedUser = userRepository.save(user);

        String uuid = UUID.randomUUID().toString();
        EmailVerify emailVerify = emailVerifyMapper.createEmailVerify(uuid, savedUser);
        emailVerifyRepository.save(emailVerify);

        sendVerificationEmail(dto.getEmail(), uuid);
        log.info("회원가입 완료 - 사용자: {}, UUID: {}", dto.getEmail(), uuid);
    }

    // 추가: 프로필 수정 메서드
    @Transactional
    public UserDto.Response updateProfile(Integer userId, UserDto.UpdateProfile dto) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 닉네임이 변경되었을 때만 중복 체크
        if (dto.getNickname() != null && !dto.getNickname().equals(user.getNickname())) {
            // 닉네임 유효성 검사
            String trimmedNickname = dto.getNickname().trim();
            if (trimmedNickname.length() < 2 || trimmedNickname.length() > 20) {
                throw new IllegalArgumentException("닉네임은 2자 이상 20자 이하로 입력해주세요.");
            }

            // 중복 체크 (다른 사용자가 사용 중인지)
            if (userRepository.findByNickname(trimmedNickname).isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }

            log.info("닉네임 중복 체크 통과: {} -> {}", user.getNickname(), trimmedNickname);
        } else {
            log.info("닉네임 변경 없음 - 중복 체크 건너뛰기");
        }

        // 3. MapStruct를 사용한 Entity 업데이트
        userMapper.updateEntityFromDto(user, dto);

        // 4. 매핑 후 결과 확인

        // 5. 데이터베이스 저장
        User savedUser = userRepository.save(user);


        // 6. Response DTO 변환
        UserDto.Response response = userMapper.entityToResponse(savedUser);

        return response;
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

    private void sendVerificationEmail(String email, String uuid) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("[PickCook] 이메일 인증을 완료해주세요");

        String htmlContent = buildEmailContent(uuid);
        helper.setText(htmlContent, true);

        emailSender.send(message);
    }

    private String buildEmailContent(String uuid) {
        return "<h2 style='color: #2e6c80;'>PickCook 가입을 환영합니다!</h2>"
                + "<p>아래 링크를 클릭하여 이메일 인증을 완료해주세요:</p>"
                + "<a href='http://localhost:8080/api/user/verify?uuid=" + uuid + "' "
                + "style='background-color: #2e6c80; color: white; padding: 10px 20px; "
                + "text-decoration: none; border-radius: 5px;'>이메일 인증하기</a>"
                + "<p><small>이 링크는 24시간 후 만료됩니다.</small></p>";
    }
}