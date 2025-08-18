package org.example.be17pickcook.config;

import org.example.be17pickcook.config.filter.JwtAuthFilter;
import org.example.be17pickcook.config.filter.LoginFilter;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.config.oauth.OAuth2AuthenticationSuccessHandler;
import org.example.be17pickcook.domain.user.mapper.UserMapper;
import org.example.be17pickcook.domain.user.service.OAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenticationConfiguration configuration;
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final UserMapper userMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")); // 수정: PATCH 추가!
        configuration.setAllowedHeaders(List.of("*"));

        // 🔧 핵심 추가: 쿠키/인증 정보 허용
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // @Component vs @Bean
    // @Component : 프로젝트가 실행될 때 컴포넌트 스캔을 통해서 객체를 생성해서 스프링 컨테이너에 Bean으로 등록
    //              개발자가 직접 개발한 클래스의 객체를 등록할 때 사용
    // @Bean : 컴포넌트 스캔 X, 개발자가 직접 객체를 생성해서 스프링 컨테이너에 Bean으로 등록
    //              라이브러리를 가져와서 라이브러리의 객체를 등록할 때 사용
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.oauth2Login(config -> {
                    config.userInfoEndpoint(
                            endpoint ->
                                    endpoint.userService(oAuth2UserService)
                    );
                    config.successHandler(oAuth2AuthenticationSuccessHandler);
                }
        );

        http.authorizeHttpRequests(
                (auth) -> auth
                        .requestMatchers(
                                "/login",
                                "/api/user/signup",          // ✅ 수정: 올바른 경로
                                "/api/user/verify",          // ✅ 추가: 이메일 인증
                                "/api/user/check-email",     // ✅ 추가: 이메일 중복체크
                                "/api/user/find-email",      // ✅ 추가: 아이디 찾기
                                "/api/user/request-password-reset", // ✅ 추가: 비밀번호 재설정 요청
                                "/api/user/reset-password",  // ✅ 추가: 비밀번호 재설정
                                "/oauth2/authorization/kakao" // ✅ OAuth2 카카오 로그인
                        ).permitAll()
                        .requestMatchers("/test/*").hasRole("USER") // 특정 권한(USER)이 있는 사용자만 허용
//                        .requestMatchers("/test/*").authenticated() // 로그인한 모든 사용자만 허용
//                        .anyRequest().authenticated()
                        .anyRequest().permitAll()
        );
        http.cors(cors ->
                cors.configurationSource(corsConfigurationSource()));

        http.csrf(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);

        http.addFilterBefore(new JwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        LoginFilter loginFilter = new LoginFilter(configuration.getAuthenticationManager(), userMapper);
        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
