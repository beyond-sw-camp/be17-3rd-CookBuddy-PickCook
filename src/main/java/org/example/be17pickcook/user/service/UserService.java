package org.example.be17pickcook.user.service;

import org.example.be17pickcook.user.model.EmailVerify;
import org.example.be17pickcook.user.model.User;
import org.example.be17pickcook.user.model.UserDto;
import org.example.be17pickcook.user.repository.EmailVerifyRepository;
import org.example.be17pickcook.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final JavaMailSender emailSender;
    private final EmailVerifyRepository emailVerifyRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> result =  userRepository.findByEmail(username);

        if(result.isPresent()) {
            User user = result.get();
            return UserDto.AuthUser.from(user);
        }

        return null;
    }

    public void signup(UserDto.Register dto) throws MessagingException {
        User user = userRepository.save(dto.toEntity());
        // 메일 전송
        String uuid = UUID.randomUUID().toString();

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(dto.getEmail());
        String subject = "[내 사이트] 가입 환영";
        String htmlContent = "<h2 style='color: #2e6c80;'>가입을 환영합니다!</h2>"
                + "<p>아래 링크를 클릭하여 이메일 인증을 완료해주세요:</p>"
                + "<a href='http://localhost:8080/api/user/verify?uuid=" + uuid + "'>이메일 인증하기</a>";
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        emailSender.send(message);

        // 랜덤한 값을 DB에 저장
        EmailVerify emailVerify= EmailVerify.builder()
                .uuid(uuid)
                .user(user)
                .build();
        emailVerifyRepository.save(emailVerify);
    }

    public void verify(String uuid) {
        Optional<EmailVerify> result = emailVerifyRepository.findByUuid(uuid);

        if(result.isPresent()) {
            EmailVerify emailVerify = result.get();
            User user = emailVerify.getUser();
            user.userVerify();
            userRepository.save(user);
        } else {
            System.out.println("인증 실패");
        }
    }
}
