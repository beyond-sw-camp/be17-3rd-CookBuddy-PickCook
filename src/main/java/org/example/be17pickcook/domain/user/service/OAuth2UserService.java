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
import java.util.UUID;

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
        String kakaoId = attributes.get("id").toString();

        // 수정: properties null 체크 추가
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        String nickname = null;

        // 추가 디버깅
        log.info("=== 디버깅 시작 ===");
        log.info("attributes: {}", attributes);
        log.info("properties: {}", properties);

        // properties와 nickname 안전한 추출
        if (properties != null && properties.get("nickname") != null) {
            nickname = (String) properties.get("nickname");
            log.info("카카오에서 받은 nickname: {}", nickname);
        } else {
            log.warn("properties가 null이거나 nickname이 없음");
        }

        // 추가: nickname이 null이거나 빈 문자열인 경우 기본값 설정
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = "카카오사용자" + kakaoId.substring(Math.max(0, kakaoId.length() - 4));
            log.info("기본 nickname 생성: {}", nickname);
        }

        // 추가: 매퍼 호출 전 최종 확인
        log.info("최종 nickname 값: '{}' (null 여부: {})", nickname, nickname == null);
        log.info("=== 디버깅 끝 ===");

        // 나머지 코드는 동일...
        Optional<User> existingUser = userRepository.findByEmail(kakaoId);
        User user;

        if (existingUser.isEmpty()) {
            // MapStruct 매퍼로 OAuth2 사용자 생성 (일단 nickname 값 넣음)
            user = userMapper.createOAuth2User(kakaoId, nickname);

            // name 기본값 설정
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName("이름을 입력해주세요");
            }

            if (user.getPhone() == null || user.getPhone().isBlank()) {
                user.setPhone("이름을 입력해주세요");
            }

            // nickname 유니크 보장 (랜덤 suffix 붙이기)
            user.setNickname(generateUniqueNickname(nickname));

            user = userRepository.save(user);
            log.info("OAuth2 신규 사용자 생성 - 카카오ID: {}, 닉네임: {}", kakaoId, user.getNickname());
        } else {
            user = existingUser.get();
            log.info("OAuth2 기존 사용자 로그인 - 카카오ID: {}", kakaoId);
        }

        return userMapper.entityToAuthUserWithAttributes(user, attributes);
    }

    /**
     * 닉네임 중복 방지 로직
     * ex) nickname → nickname_ab12, nickname_f9x3 ...
     */
    private String generateUniqueNickname(String baseNickname) {
        String candidate = baseNickname;
        while (userRepository.existsByNickname(candidate)) {
            String randomSuffix = UUID.randomUUID().toString().substring(0, 4); // 4자리 랜덤
            candidate = baseNickname + "_" + randomSuffix;
        }
        return candidate;
    }
}