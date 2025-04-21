package com.runinto.config.filter;

import com.runinto.auth.domain.CustomUserDetails;
import com.runinto.user.domain.Gender;
import com.runinto.user.domain.User;
import com.runinto.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //request에서 Authorization 헤더를 찾음
        String authorization= request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {

            System.out.println("token null");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        String token = authorization.split(" ")[1];

        log.info(token);
        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {

            System.out.println("token expired");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }


        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        User userEntity =new User(1L, "test", "IMGURL", "여자", Gender.MALE, 20,"admin","1234", "test");
        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

        log.info("1차");
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        log.info("2차");
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.info("3차");
        filterChain.doFilter(request, response);
    }
}
