package org.example.be17pickcook.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.domain.user.mapper.UserMapper;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // import 수정

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper; // MapStruct 매퍼 주입

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 카카오 사용자 정보 추출
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        String nickname = (String) properties.get("nickname");
        String kakaoId = attributes.get("id").toString();

        Optional<User> existingUser = userRepository.findByEmail(kakaoId);
        User user;

        if (existingUser.isEmpty()) {
            // MapStruct 매퍼로 OAuth2 사용자 생성
            user = userMapper.createOAuth2User(kakaoId, nickname);
            user = userRepository.save(user);
            log.info("OAuth2 신규 사용자 생성 - 카카오ID: {}, 닉네임: {}", kakaoId, nickname);
        } else {
            user = existingUser.get();
            log.info("OAuth2 기존 사용자 로그인 - 카카오ID: {}", kakaoId);
        }

        // MapStruct 매퍼로 OAuth2 속성까지 한 번에 처리
        return userMapper.entityToAuthUserWithAttributes(user, attributes);
    }
}