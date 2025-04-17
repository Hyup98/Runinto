package com.runinto.config;

import com.runinto.auth.service.AuthService;
import com.runinto.user.domain.repository.UserMemoryRepository;
import com.runinto.user.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfig {

    @Bean
    public AuthService authService() {
        return new AuthService(userService());
    }

    @Bean
    public UserService userService() {
        return new UserService(userMemoryRepository()); // 예시로 필요하면 추가
    }

    @Bean
    public UserMemoryRepository userMemoryRepository() { return new UserMemoryRepository();}
}
