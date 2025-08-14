package org.example.be17pickcook.config.filter;

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


    // 그림에서 9번 로직
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        System.out.println("LoginFilter 성공 로직.");
        UserDto.AuthUser authUser = (UserDto.AuthUser) authResult.getPrincipal();

        String jwt = JwtUtil.generateToken(authUser.getEmail(), authUser.getIdx());

        if (jwt != null) {
            Cookie cookie = new Cookie("PICKCOOK_AT", jwt);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);

            UserDto.Response responseDto = userMapper.authUserToResponse(authUser);
            response.getWriter().write(new ObjectMapper().writeValueAsString(responseDto));
        }
    }
}

