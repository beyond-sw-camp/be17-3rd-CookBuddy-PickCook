package org.example.be17pickcook.config.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.domain.user.mapper.UserMapper;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public LoginFilter(AuthenticationManager authenticationManager, UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        super.setFilterProcessesUrl("/api/auth/login"); // 경로 설정
    }

    // 원래는 form-data 형식으로 사용자 정보를 입력받았는데
    // 우리는 JSON 형태로 입력을 받기 위해서 재정의
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/login/oauth2/code/")) {
            throw new AuthenticationException("OAuth2 callback should not be processed by LoginFilter") {};
        }

        UsernamePasswordAuthenticationToken authToken;
        try {

            UserDto.Login dto = new ObjectMapper().readValue(request.getInputStream(), UserDto.Login.class);

            log.info("로그인 시도: 이메일 = {}", dto.getEmail());

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

        UserDto.AuthUser authUser = (UserDto.AuthUser) authResult.getPrincipal();

        // SecurityContext에 인증 정보 세팅
        SecurityContextHolder.getContext().setAuthentication(authResult);

        log.info("로그인 성공: 사용자 = {}", authUser.getEmail());

        String jwt = JwtUtil.generateToken(authUser.getEmail(), authUser.getIdx(), authUser.getNickname(), authUser.getName());

        if (jwt != null) {
            // 🔧 수정: 기본 쿠키 설정 제거하고 헤더로만 설정

            // 🔧 수정: Set-Cookie 헤더 직접 설정 (SameSite=Lax 추가)
            String cookieValue = String.format(
                    "PICKCOOK_AT=%s; Path=/; HttpOnly; Max-Age=7200; SameSite=Lax",
                    jwt
            );

            response.setHeader("Set-Cookie", cookieValue);


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
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {

        log.warn("로그인 실패: 원인 = {}", failed.getMessage());

        // BaseResponse 형식으로 에러 응답
        BaseResponse<Void> errorResponse = BaseResponse.error(BaseResponseStatus.INVALID_USER_INFO);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }
}

