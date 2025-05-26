package com.runinto.config;

import com.runinto.auth.domain.CustomUserDetails;
import com.runinto.config.filter.JWTFilter;
import com.runinto.config.filter.LoginFilter;
import com.runinto.util.JWTUtil;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // CSRF 간결화

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;

    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**", // 이 부분이 가장 중요합니다.
            "/swagger-resources/**",
            "/webjars/**"
    };

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil) {

        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 비활성화 (Stateless API 또는 세션 기반이라도 특정 상황에 따라)
                .csrf(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 비활성화 (SessionFilter를 사용하므로)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 폼 로그인 비활성화 (SessionFilter를 사용하므로)
                .formLogin(AbstractHttpConfigurer::disable)

                // 요청에 대한 접근 권한 설정: 모든 요청을 일단 허용 (실제 권한 검사는 SessionFilter에서)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // SessionFilter가 먼저 동작하므로 여기서는 모든 요청 허용
                )

                // 세션 관리: STATELESS로 설정 (SessionFilter에서 세션을 사용하더라도,
                // Spring Security 자체의 세션 생성 및 사용을 최소화하려는 의도일 수 있음.
                // 만약 SessionFilter가 HttpSession을 적극적으로 사용하고 Spring Security가 이를 인지해야 한다면,
                // 이 부분은 SessionCreationPolicy.IF_REQUIRED 등으로 변경하거나 SessionFilter 로직과 맞출 필요가 있음)
                // 하지만 현재 SessionFilter는 getSession(false)를 사용하므로, 이 설정과 크게 충돌하지 않을 수 있음.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // H2 콘솔 사용을 위한 Frame Options 설정 (개발 환경에서만 필요)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                );


        return http.build();
    }
}