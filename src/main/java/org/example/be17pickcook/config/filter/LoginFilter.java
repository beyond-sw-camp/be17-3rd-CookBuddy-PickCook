package org.example.be17pickcook.config.filter;

import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.domain.user.mapper.UserMapper;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

//@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public LoginFilter(AuthenticationManager authenticationManager, UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        setFilterProcessesUrl("/login"); // 경로 설정
    }

    // 원래는 form-data 형식으로 사용자 정보를 입력받았는데
    // 우리는 JSON 형태로 입력을 받기 위해서 재정의
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        UsernamePasswordAuthenticationToken authToken;
        try {
            System.out.println("LoginFilter 실행됐다.");

            UserDto.Login dto = new ObjectMapper().readValue(request.getInputStream(), UserDto.Login.class);
            authToken = new UsernamePasswordAuthenticationToken(
                    dto.getEmail(), dto.getPassword(), null
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 그림에서 3번 로직
        return authenticationManager.authenticate(authToken);
    }


    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        System.out.println("=== LoginFilter 성공 로직 시작 ===");
        UserDto.AuthUser authUser = (UserDto.AuthUser) authResult.getPrincipal();
        System.out.println("인증된 사용자: " + authUser.getEmail());

        String jwt = JwtUtil.generateToken(authUser.getEmail(), authUser.getIdx(), authUser.getNickname());
        System.out.println("생성된 JWT: " + (jwt != null ? "성공" : "실패"));

        if (jwt != null) {
            // 🔧 수정: 기본 쿠키 설정 제거하고 헤더로만 설정

            // 🔧 수정: Set-Cookie 헤더 직접 설정 (SameSite=Lax 추가)
            String cookieValue = String.format(
                    "PICKCOOK_AT=%s; Path=/; HttpOnly; Max-Age=7200; SameSite=Lax",
                    jwt
            );

            response.setHeader("Set-Cookie", cookieValue);
            System.out.println("🍪 Set-Cookie 헤더 설정: " + cookieValue);

            // 🔧 추가: CORS 헤더 명시적 설정
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");

            // BaseResponse 형식으로 JSON 응답
            UserDto.Response responseDto = userMapper.authUserToResponse(authUser);
            BaseResponse<UserDto.Response> baseResponse = new BaseResponse<>(
                    true,
                    BaseResponseStatus.LOGIN_SUCCESS.getCode(),
                    BaseResponseStatus.LOGIN_SUCCESS.getMessage(),
                    responseDto
            );

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(baseResponse));
            System.out.println("=== LoginFilter 성공 로직 완료 ===");
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        System.out.println("LoginFilter 실패 로직.");

        // BaseResponse 형식으로 에러 응답
        BaseResponse<Void> errorResponse = BaseResponse.error(BaseResponseStatus.INVALID_USER_INFO);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }
}

