package org.example.be17pickcook.domain.user.service;

import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuth2UserSerivce extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        Map properties = ((Map) attributes.get("properties"));
        String nickname = (String) properties.get("nickname");
        String kakaoId = attributes.get("id").toString();

        Optional<User> socialUserResult =
                userRepository.findByEmail(kakaoId);
        User user;
        if (!socialUserResult.isPresent()) {
            user = userRepository.save(
                    User.builder()
                            .nickname(nickname)
                            .email(kakaoId)
                            .enabled(true)
                            .build()
            );
        } else {
            user = socialUserResult.get();
        }
        return UserDto.AuthUser.from(user);
    }
}
